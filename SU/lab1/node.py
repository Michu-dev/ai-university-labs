class Node(object):
	def __init__(self, feature_index=None, threshold=None, left=None, right=None, var_red=None, value=None):
		self.left = left # Typ: Node, wierzchołek znajdujący się po lewej stornie
		self.right = right # Typ: Node, wierzchołek znajdujący się po prawej stornie
		self.feature_index = feature_index # atrybut (numer), według którego wykonywany jest podział
		self.threshold = threshold # wartość, według której wykonywany jest podział
		self.value = value # wynik (występuje tylko w liściach)
		self.var_red = var_red