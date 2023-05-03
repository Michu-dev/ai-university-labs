import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object KafkaReceiver {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      .appName("KafkaReceiver")
      //      .master("local[2]")
      .getOrCreate()

    import spark.implicits._
    val CLUSTER_NAME = args(0)
    val SOURCE_TOPIC = args(1)
    val TARGET_TABLE = args(2)

    val dataSchema = StructType(
      List(
        StructField("house", StringType, true),
        StructField("character", StringType, true),
        StructField("score", StringType, true),
        StructField("ts", TimestampType, true)
      )
    )

    val ds1 = spark.readStream.
      format("kafka").
      option("kafka.bootstrap.servers", s"${CLUSTER_NAME}-w-0:9092").
      option("subscribe", SOURCE_TOPIC).
      load()

    val valueDF = ds1.select(expr("CAST(value AS STRING)").as("value"))

    val dataDF = valueDF.select(
      from_json($"value".cast(StringType), dataSchema).as("val")).
      select($"val.house",$"val.character",
        $"val.score".cast("int").as("score"),$"val.ts")

    val resultDF = dataDF.groupBy($"house").
      agg(count($"score").as("how_many"), sum($"score").as("sum_score"),
        approx_count_distinct($"character").as("no_characters"))

    val streamWriter = resultDF.writeStream.outputMode("complete").foreachBatch {
      (batchDF: DataFrame, batchId: Long) =>
        batchDF.write.
          format("jdbc").
          mode(SaveMode.Overwrite).
          option("url", s"jdbc:postgresql://${CLUSTER_NAME}-m:8432/streamoutput").
          option("dbtable", TARGET_TABLE).
          option("user", "postgres").
          option("password", "mysecretpassword").
          option("truncate", "true").
          save()
    }

    val query = streamWriter.start()



  }
}
