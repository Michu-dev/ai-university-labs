import numpy as np


# INFORMACJE DO ROZWIĄZANIA: Zaimplementowany algorytm wyszukuje najmniejszą możliwą wariancję po danym podziale spośród wszystkich możliwych podziałów (rozwiązań) na danym
# poziomie. Poniżej ustawiona jest wartość maksymalnej wysokości drzewa, służąca za warunek stopu w rekurencyjnym budowaniu drzewa regresji. Najważniejsze parametry w klasie
# Node zostały wyjaśnione obok ich deklaracji. Rozwiązanie poza metodą best_split, gdzie wykorzystana jest wariancja jako funkcja ewaluacji, nie wyróżnia się niczym szczególnym. 

MAX_DEPTH = 5

class Node(object):
	def __init__(self):
		self.left = None # Typ: Node, wierzchołek znajdujący się po lewej stornie
		self.right = None # Typ: Node, wierzchołek znajdujący się po prawej stornie
		self.feature_index = None # atrybut (numer), według którego wykonywany jest podział
		self.threshold = None # wartość, według której wykonywany jest podział
		self.value = None # wynik (występuje tylko w liściach)

	def calc_variance(self, data: list, nAttr: int):
		y = data[:, nAttr]
		return np.var(y)
	
	def best_split(self, data, nAttr):
		current_variance = self.calc_variance(data, nAttr)
		best_variance = current_variance
		feature_index = None
		threshold = None

		for y in range(nAttr):
			feature_values = np.unique(data[:, y])
			for feature_value in feature_values:
				d1 = data[data[:, y] <= feature_value]
				d2 = data[data[:, y] > feature_value]
				if len(d1) > 0 and len(d2) > 0:
					modified_variance = len(d1) / len(data) * self.calc_variance(d1, nAttr) + len(d2) / len(data) * self.calc_variance(d2, nAttr)
					if modified_variance < best_variance:
						best_variance = modified_variance
						feature_index = y
						threshold = feature_value
		
		return feature_index, threshold

	def perform_split(self, data, nAttr, recurrence_level):
		feature_index, threshold = self.best_split(data, nAttr)

		if feature_index is not None and recurrence_level < MAX_DEPTH:
			d1 = data[data[:, feature_index] <= threshold]
			d2 = data[data[:, feature_index] > threshold]

			self.feature_index = feature_index
			self.threshold = threshold

			self.left = Node()
			self.right = Node()
			self.left.perform_split(d1, nAttr, recurrence_level + 1)
			self.right.perform_split(d2, nAttr, recurrence_level + 1)
		else:
			self.value = np.mean(data[:, nAttr])
		
	
	def predict(self, example):
		if self.feature_index is not None:
			if example[self.feature_index] > self.threshold:
				return self.right.predict(example)
			else:
				return self.left.predict(example)
		return self.value		
		
		
		
# Najprostsze wczytywanie i zapisywanie danych, a także wywołanie obiektu Node	
# Szczególnie przydatne dla osób nie znających numpy
# Zbiór danych jest reprezentowany jako "lista list". 
# tj. data[0] zwróci listę z wartościami cech pierwszego przykładu
# Jeśli znasz numpy i bolą Cię od poniższego kodu oczy - możesz go zmienić
# Jeśli nie znasz numpy - skorzystaj z kodu, dokończ zadanie... i naucz sie numpy. W kolejnych tygodniach będziemy z niego korzystać.

for file_id in range(1, 14):
	id = f"./data/{file_id}" 
	data = []
	y = [line.strip() for line in open(id + '-Y.csv')]
	for i, line in enumerate(open(id + '-X.csv')):
		if i == 0:
			continue
		x = [float(j) for j in line.strip().split(',')]
		nAttr = len(x)
		x.append(float(y[i]))
		data.append(x)
	data = np.array(data, dtype=np.float64)
	print(f"Data {file_id} load complete!")
	tree_root = Node()
	tree_root.perform_split(data, nAttr, 0)
	print(f"Training {file_id} complete!")

	with open(id+'.csv', 'w') as f:
		for i, line in enumerate(open(id + '-test.csv')):
			if i == 0:
				continue
			x = [float(j) for j in line.strip().split(',')]
			y = tree_root.predict(x)
			f.write(str(y))
			f.write('\n')
