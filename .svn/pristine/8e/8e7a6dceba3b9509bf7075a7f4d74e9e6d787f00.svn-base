package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

class Node<N,E> {
	public final N obj;
	HashMap<Node<N,E>,HashSet<E>> neighbors;
	
	public Node(N o) {
		obj = o;
		neighbors = new HashMap<>();
	}
	
	protected void addNeighbor(Node<N,E> n, E e){
		HashSet<E> edges = neighbors.get(n);
		if(edges == null) {
			edges = new HashSet<>();
			neighbors.put(n, edges);
		}
		edges.add(e);
	}
	
	public int hashCode() {
		return obj.hashCode();
	}

	public boolean equals(Object obj) {
		if(obj instanceof Node){
			return ((Node<?,?>)obj).obj.equals(this.obj);
		}
		return false;
	}
	
	protected void removeAllEdges(){
		for(Node<N,E> n : neighbors.keySet())
			n.neighbors.remove(this);
		neighbors.clear();
	}
	
	public String toString() {
		return "Node: "+obj.toString();
	}
	
}

public class Graph<N,E> {	
	HashMap<N,Node<N,E>> graph;
	
	public Graph() {
		graph = new HashMap<>();
	}
	
	public void removeNode(N node){
		Node<N,E> n = graph.remove(node);
		n.removeAllEdges();
	}
	
	/**
	 * add node if it isn't already in graph (by HashMap containsKey)
	 * 
	 * @param e
	 * @return true if node was successfully added, false if it already exists
	 */
	public boolean add(N e){
		Node<N,E> n = new Node<>(e);
		if(!graph.containsKey(e)){
			graph.put(e,n);
			return true;
		}
		return false;
	}
	
	public HashSet<E> getEdges(N n){
		HashSet<E> r = new HashSet<E>();
		for(HashSet<E> t : graph.get(n).neighbors.values())
			r.addAll(t);
		return r;
	}
	
	public void addEdge(N n1, N n2, E e){
		Node<N,E> nd1 = graph.get(n1);
		Node<N,E> nd2 = graph.get(n2);
		if(nd1 == null || nd2 == null)
			throw new RuntimeException("node(s) do not exist:\n e1="+n1+" -> "+nd1+"\ne2="+n2+" -> "+nd2);
		nd1.addNeighbor(nd2,e);
		nd2.addNeighbor(nd1,e);
	}
	
	/**
	 * adds adge from n1 to n2
	 * @param n1
	 * @param n2
	 * @param e
	 */
	public void addDirectedEdge(N n1, N n2, E e){
		Node<N,E> nd1 = graph.get(n1);
		Node<N,E> nd2 = graph.get(n2);
		if(nd1 == null || nd2 == null)
			throw new RuntimeException("node(s) do not exist:\n e1="+n1+" -> "+nd1+"\ne2="+n2+" -> "+nd2);
		nd1.addNeighbor(nd2,e);
	}
	
	/**
	 * expect undrected grapch
	 * @return
	 */
	public HashMap<N, Integer> connectedComponents(){
		HashMap<N, Integer> r = new HashMap<>();
		ArrayList<Node<N,E>> allnodes = new ArrayList<>(graph.values());
		HashMap<Node<N,E>,Integer> allnodesMap = new HashMap<>(allnodes.size());
		for(int i=0;i<allnodes.size();i++)
			allnodesMap.put(allnodes.get(i), i);
		LinkedList<Node<N,E>> cnodes = new LinkedList<>();
		int[] index = new int[]{0};
		int compno = 0;
		for(;;){		
			if(cnodes.size() == 0){
				Node<N,E> f = findFirstNotNull(allnodes,index);
				if(f == null)
					break;
				allnodesMap.remove(f);
				cnodes.add(f);
				compno++;
			}else{
				Node<N,E> n = cnodes.removeFirst();
				r.put(n.obj, compno);
				for(Node<N,E> nj : n.neighbors.keySet()){
					Integer inx = allnodesMap.remove(nj);
					if(inx != null){
						cnodes.add(nj);
						allnodes.set(inx,null);
					}
				}
			}
		}
		return r;
	}
	
	private <X> X findFirstNotNull(ArrayList<X> l,int[] i){
		for(;i[0]<l.size();i[0]++){
			if(l.get(i[0]) != null){
				X t = l.get(i[0]);
				l.set(i[0], null);
				return t;
			}
		}
		return null;
	}
	
	/**
	 * Expects that graph do not have cycles
	 * @param from
	 * @param to
	 * @return
	 */
	public ArrayList<ArrayList<E>> getPaths(N from, N to){
		ArrayList<ArrayList<E>> r = new ArrayList<>();
		findPaths(graph.get(from), graph.get(to),r,new ArrayList<E>());
		return r;
	}
	
	private void findPaths(Node<N,E> from,Node<N,E> to, ArrayList<ArrayList<E>> r,ArrayList<E> c){
		if(from.equals(to)){
			r.add(c);
			return;
		}
		
		for(Node<N,E> nj : from.neighbors.keySet()){
			HashSet<E> edges = from.neighbors.get(nj);
			for(E e : edges){
				@SuppressWarnings("unchecked")
				ArrayList<E> cc = (ArrayList<E>) c.clone();
				cc.add(e);
				findPaths(nj,to,r,cc);
			}
		}
	}
	
	public static void testGetPaths(){
		Graph<Integer,Integer> g = new Graph<>();
		for(int i =1;i<=9;i++)
			g.add(i);
		g.addDirectedEdge(1, 2,1);
		g.addDirectedEdge(1, 3,2);
		g.addDirectedEdge(2, 4,3);
		g.addDirectedEdge(2, 6,4);
		g.addDirectedEdge(3, 6,5);
		g.addDirectedEdge(3, 7,6);
		g.addDirectedEdge(4, 5,7);
		g.addDirectedEdge(6, 5,8);
		g.addDirectedEdge(6, 7,9);
		g.addDirectedEdge(7, 8,10);
		g.addDirectedEdge(9, 8,11);
		ArrayList<ArrayList<Integer>> r = g.getPaths(1, 8);
		for(int i =0;i<r.size();i++){
			System.out.print("path "+(i+1)+":\t");
			for(Integer j : r.get(i))
				System.out.print(j+",");
			System.out.println();
		}
	}
	
	public static void  testConnectedComponents(){
		Graph<Integer,Integer> g = new Graph<>();
		for(int i =0;i<=10;i++)
			g.add(i);
		for(int i=0;i<=10;i++)
			for(int j=i+1;j<=10;j++)
				if(i%3 == j%3)
					g.addEdge(i, j,i%3);
		HashMap<Integer,Integer> comp = g.connectedComponents();
		ArrayList<Integer> els = new ArrayList<>(comp.keySet());
		Collections.sort(els);
		for(int e : els)
			System.out.println(e+": "+comp.get(e));
	}
	
	public static void main(String[] args) {
		testGetPaths();
	}
	
	public Set<N> getAllNodes(){
		return graph.keySet();
	}
	
	public HashSet<E> getAllEdges(){
		HashSet<E> r = new HashSet<>();
		for(Node<N,E> n : graph.values())
			for(HashSet<E> e : n.neighbors.values())
				r.addAll(e);
		return r;
	}
}
