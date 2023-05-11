import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.expr
import org.apache.spark.sql.types._

object KafkaConsument {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().
      setMaster("local").setAppName("UKTrafficAnalysis")
    val sc: SparkContext = new SparkContext(conf)

    val spark = SparkSession.builder()
      .master("local[2]")
      .appName("UKTrafficAnalysis")
      .getOrCreate()

    val dataSchema = StructType(
      List(
        StructField("house", StringType, true),
        StructField("character", StringType, true),
        StructField("score", StringType, true),
        StructField("ts", TimestampType, true)
      )
    )
    val cluster_name = "my-cluster"

    val ds1 = spark.
      readStream.
      format("kafka").
      option("kafka.bootstrap.servers", s"${cluster_name}-w-0:9092").
      option("subscribe", "kafka-input").
      load()
    val valueDF = ds1.select(expr("CAST(value AS STRING)").as("value"))

    val dataDF = valueDF.select(
      from_json($"value".cast(StringType), dataSchema).as("val")).
      select($"val.house", $"val.character",
        $"val.score".cast("int").as("score"), $"val.ts")

  }
}
