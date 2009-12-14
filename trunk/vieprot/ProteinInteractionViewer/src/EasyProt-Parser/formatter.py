import sys, urllib, os, string, json, urllib2
from datetime import date, timedelta

class GraphMLGenerator:

	def __init__(self):
		self.curModule = 0
		self.organism = 0
		# Stores the nodes for each organism
		self.nodes = []
		self.goAnnotations = {}
		self.analysisAnnotations = {}
		self.internalEdges = []
		self.alignedEdges = []
		self.indentLevel = 0
		self.graphID = 0
		self.internalEdgeCounter = 0
		self.alignedEdgeCounter = 0

	def generate(self, produles, proteinOne, proteinTwo):
		f = open(produles,'r')
		for line in f:
			lineArray = line.split()
			# Ignore empty lines
			if len(lineArray) > 0:
				# Module line
				if(len(lineArray) == 3 and lineArray[1] == 'Module'):
					self.curModule = int(lineArray[2])-1
					self.nodes.append([[] for i in range(2)])
					self.internalEdges.append([[] for i in range(2)])
					self.alignedEdges.append([[] for i in range(2)])
				# Graph type
				elif(lineArray[0].find('ppiGraph') != -1):
					leftInd = lineArray[0].find('[')+1
					rightInd = lineArray[0].find(']')
					self.organism = int(lineArray[0][leftInd:rightInd])
				# Store the proteins as nodes
				elif(lineArray[0].find('protein') != -1):
					self.nodes[self.curModule][self.organism].append(str(lineArray[1]))
				# Store the internal edges
				elif(lineArray[0].find('PPI') != -1):
					# Retrieve the source and destination nodes
					edge = lineArray[1].translate(None,'()').split(',')
					# Store the edges
					self.internalEdges[self.curModule][self.organism].append((edge[0],edge[1]))
				# Store the BLAST edges
				elif(lineArray[0].find('aligned') != -1):
					# Three element tuple: (source, dest, E-value)
					edge = lineArray[1].translate(None,'()').split(',')
					# Store the edge
					self.alignedEdges[self.curModule][self.organism].append((edge[0],edge[1],edge[2]))
		f.close()

		f = open(proteinOne,'r')
		for line in f:
			lineArray = line.split('\t')
			# Ignore empty lines
			if len(lineArray) > 0:
				# Analysis annotation
				if(len(lineArray) == 3):
					self.analysisAnnotations[str(lineArray[0])] = int(lineArray[2]) 
				else:
					if((str(lineArray[0]) in self.goAnnotations) == False):
						self.goAnnotations[str(lineArray[0])] = []
					annotation = str(lineArray[2])+','+str(lineArray[3])+','+str(lineArray[4])+','+(str(lineArray[5]).strip('\n'))
					self.goAnnotations[str(lineArray[0])].append(annotation)

		f.close()

		f = open(proteinTwo,'r')
		lineNum = 0
		for line in f:
			lineArray = line.split('\t')
			# Ignore empty lines
			if len(lineArray) > 0:
				# Analysis annotation
				if(len(lineArray) == 3):
					self.analysisAnnotations[str(lineArray[0])] = int(lineArray[2]) 
				else:
					if((str(lineArray[0]) in self.goAnnotations) == False):
						self.goAnnotations[str(lineArray[0])] = []
					annotation = str(lineArray[2])+','+str(lineArray[3])+','+str(lineArray[4])+','+(str(lineArray[5]).strip('\n'))
					self.goAnnotations[str(lineArray[0])].append(annotation)

		f.close()

		for key in self.goAnnotations.keys():
			self.goAnnotations[key] = set(self.goAnnotations[key])

		# Just consider module 0 for now
		curModule = 0
		f = open("produlesXML.xml",'w')
		self.createGraph(f)
		f.close()

	def createGraph(self,f):
		self.indentLevel = 0;
		f.write(self.getGraphMLHeader())
		f.write(self.getKeys())
		for i in range(0,len(self.nodes)):
			self.writeModule(f,i)
		f.write('</graphml>')

	def getGraphMLHeader(self):
		return '''<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
'''

	def getKeys(self):
		return '''	<key id="d0" for="node" attr.name="color" attr.type="string">
		<default>yellow</default>
	</key>
	<key id="d2" for="node" attr.name="go_annotation" attr.type="string"/>
	<key id="d3" for="node" attr.name="analysis_annotation" attr.type="int"/>
	<key id="d1" for="edge" attr.name="weight" attr.type="double"/>
'''

	def writeModule(self, f, moduleNum):
		self.indentLevel += 1
		self.indent(f)
		f.write('<graph id="' + str(self.graphID) + '" edgedefault="undirected">\n')
		self.graphID += 1

		self.writeNodes(f,moduleNum)
		self.writeInternalEdges(f,moduleNum)
		self.writeAlignedEdges(f,moduleNum)

		self.indent(f)
		f.write('</graph>\n')	
		self.indentLevel -= 1

	def writeNodes(self, f, n):

		# ppiGraph[0] nodes
		nodes0 = self.nodes[n][0]
		nodes1 = self.nodes[n][1]

		for node in nodes0:
			self.writeNode(f,node,0)
		for node in nodes1:
			self.writeNode(f,node,1)

	def writeNode(self, f, identifier, groupnum):
		self.indentLevel += 1
		self.indent(f)
		f.write('<node id="'+str(identifier)+'" group="module_'+str(groupnum)+'"')
		hasAnnotation = False
		if str(identifier) in self.goAnnotations:
			hasAnnotation = True
			f.write('>\n')
			newAnnotation = ""
			for annotation in self.goAnnotations[str(identifier)]:
				newAnnotation += annotation + "|"
			newAnnotation = newAnnotation[:len(newAnnotation)-1]
			self.indentLevel += 1
			self.indent(f)
			f.write('<data key="d2">'+newAnnotation+'</data>\n')
		
		if str(identifier) in self.analysisAnnotations:
			if hasAnnotation == False:
				f.write('>\n')
				self.indentLevel += 1
			self.indent(f)
			f.write('<data key="d3">'+str(self.analysisAnnotations[str(identifier)])+'</data>\n')
			hasAnnotation = True

		if hasAnnotation == True:
			self.indentLevel -= 1
			self.indent(f)
			f.write('</node>\n')
		else:
			f.write('/>\n')
		self.indentLevel -= 1

	def writeInternalEdges(self, f, n):
		edges0 = self.internalEdges[n][0]
		edges1 = self.internalEdges[n][1]
		for edge in edges0:
			self.writeEdge(f,'e'+str(self.internalEdgeCounter),edge[0],edge[1], 'module_0')
			self.internalEdgeCounter += 1
		for edge in edges1:
			self.writeEdge(f,'e'+str(self.internalEdgeCounter),edge[0],edge[1], 'module_1')
			self.internalEdgeCounter += 1

	def writeAlignedEdges(self, f, n):
		edges0 = self.alignedEdges[n][0]
		edges1 = self.alignedEdges[n][1]
		for edge in edges0:
			self.writeAlignedEdge(f,'ea'+str(self.alignedEdgeCounter),edge[0],edge[1],edge[2], 'aligned')
			self.alignedEdgeCounter += 1
		for edge in edges1:
			self.writeAlignedEdge(f,'ea'+str(self.alignedEdgeCounter),edge[0],edge[1],edge[2], 'aligned')
			self.alignedEdgeCounter += 1

	def writeEdge(self, f, id, source, target, groupnum):
		self.indentLevel += 1
		self.indent(f)
		f.write('<edge id="'+str(source)+'_'+str(target)+'" source="'+str(source)+'" target="'+str(target)+'" group="'+str(groupnum)+'"/>\n')
		self.indentLevel -= 1

	def writeAlignedEdge(self, f, id, source, target, weight, groupnum):
		self.indentLevel += 1
		self.indent(f)
		f.write('<edge id="'+str(source)+'_'+str(target)+'" source="'+str(source)+'" target="'+str(target)+'" group="'+str(groupnum)+'">\n')
		self.indentLevel += 1
		self.indent(f)
		f.write('<data key="d1">'+str(weight)+'</data>\n')
		self.indentLevel -= 1
		self.indent(f)
		f.write('</edge>\n')
		self.indentLevel -= 1

	def indent(self, f):
		for i in range(0,self.indentLevel):
			f.write('\t')

if __name__ == "__main__":
	#test()
	#cooccurrence(sys.argv[1:])
	generator = GraphMLGenerator()
	generator.generate("produles5.dat", "proteinsOne3.dat", "proteinsTwo4.dat")
