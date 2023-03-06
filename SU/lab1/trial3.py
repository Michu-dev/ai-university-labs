import numpy as np
from node import Node


class Node(object):
	def __init__(self, feature_index=None, threshold=None, left=None, right=None, var_red=None, value=None, min_samples_split=2, max_depth=2):
		self.left = left # Typ: Node, wierzchołek znajdujący się po lewej stornie
		self.right = right # Typ: Node, wierzchołek znajdujący się po prawej stornie
		self.feature_index = feature_index # atrybut (numer), według którego wykonywany jest podział
		self.threshold = threshold # wartość, według której wykonywany jest podział
		self.value = value # wynik (występuje tylko w liściach)
		self.var_red = var_red # variance reduction - parametr, dzięki któremu odróżniam wartości nowotworzonych podziałów
		self.min_samples_split = min_samples_split # minimalna liczba podziałów (warunek stopu, parametr opcjonalny)
		self.max_depth = max_depth # maksymalny stopień rekurencji (warunek stopu, parametr opcjonalny)
        

	def split(self, data, feature_index, threshold):
		d1 = np.array([row for row in data if row[feature_index] <= threshold])
		d2 = np.array([row for row in data if row[feature_index] > threshold])
		return d1, d2
	
	def variance_reduction(self, parent, l_child, r_child):
		weight_l = len(l_child) / len(parent)
		weight_r = len(r_child) / len(parent)
		reduction = np.var(parent) - (weight_l * np.var(l_child) + weight_r * np.var(r_child))
		return reduction

	def get_best_split(self, data, num_samples, num_features):
		best_split = {}
		max_var_red = -float("inf")
		for feature_index in range(num_features):
			feature_values = data[:, feature_index]
			possible_thresholds = np.unique(feature_values)
			for threshold in possible_thresholds:
				d1, d2 = self.split(data, feature_index, threshold)
				if len(d1) > 0 and len(d2) > 0:
					y, left_y, right_y = data[:, -1], d1[:, -1], d2[:, -1]
					curr_var_red = self.variance_reduction(y, left_y, right_y)
					if curr_var_red > max_var_red:
						best_split["feature_index"] = feature_index
						best_split["threshold"] = threshold
						best_split["d1"] = d1
						best_split["d2"] = d2
						best_split["var_red"] = curr_var_red
						max_var_red = curr_var_red
		return best_split

	def perform_split(self, data, curr_depth):
		X, Y = data[:,:-1], data[:,-1]
		num_samples, num_features = np.shape(X)
		best_split = {}
		if num_samples >= self.min_samples_split and curr_depth <= self.max_depth:
			best_split = self.get_best_split(data, num_samples, num_features)
			if best_split["var_red"] > 0:
				self.left = Node()
				self.right = Node()
				self.left.perform_split(best_split["d1"], curr_depth + 1)
				self.right.perform_split(best_split["d2"], curr_depth + 1)
				self.feature_index = best_split["feature_index"]
				self.threshold = best_split["threshold"]
				self.var_red = best_split["var_red"] 
				
		self.value = np.mean(Y)
		# Znajdź najlepszy podział data
		# if uzyskano poprawę funkcji celu (bądź inny, zaproponowany przez Ciebie warunek):
			#podziel dane na dwie części d1 i d2, zgodnie z warunkiem
			#self.left = Node()
			#self.right = Node()
			#self.left.perform_split(d1)
			#self.right.perform_split(d2)
		#else:
			#obecny Node jest liściem, zapisz jego odpowiedź

	
	def predict(self, example):
		if self.feature_index is not None:
			if example[self.feature_index] > self.threshold:
				self.right.predict(example)
			else:
				self.left.predict(example)
		return self.value
		"""
		if not Node jest liściem:
			if warunek podziału jest spełniony:
				return self.right.predict(example)
			else:
				return self.left.predict(example)
		return zwróć wartość (Node jest liściem)
		"""
		
		
		
		
# Najprostsze wczytywanie i zapisywanie danych, a także wywołanie obiektu Node	
# Szczególnie przydatne dla osób nie znających numpy
# Zbiór danych jest reprezentowany jako "lista list". 
# tj. data[0] zwróci listę z wartościami cech pierwszego przykładu
# Jeśli znasz numpy i bolą Cię od poniższego kodu oczy - możesz go zmienić
# Jeśli nie znasz numpy - skorzystaj z kodu, dokończ zadanie... i naucz sie numpy. W kolejnych tygodniach będziemy z niego korzystać.

for file_id in range(1, 14):
	id = f"./data/{file_id}" # podaj id zbioru danych który chcesz przetworzyć np. 1
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
	tree_root.perform_split(data, 0)
	print(f"Training {file_id} complete!")

	with open(id+'.csv', 'w') as f:
		for i, line in enumerate(open(id + '-test.csv')):
			if i == 0:
				continue
			x = [float(j) for j in line.strip().split(',')]
			y = tree_root.predict(x)
			f.write(str(y))
			f.write('\n')