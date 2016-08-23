package test;

import de.l3s.flower.Category;

public class CategoryPair {
	public String id="";
	Category cat1= new Category();
	Category cat2=new Category();
	public Category getCat1() {
		return cat1;
	}
	public void setCat1(Category cat1) {
		this.cat1 = cat1;
	}
	public Category getCat2() {
		return cat2;
	}
	public void setCat2(Category cat2) {
		this.cat2 = cat2;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public boolean isNeighbourInFlower() {
		return isNeighbourInFlower;
	}
	public void setNeighbourInFlower(boolean isNeighbourInFlower) {
		this.isNeighbourInFlower = isNeighbourInFlower;
	}
	double score=0;
	boolean isNeighbourInFlower=false;
	public CategoryPair() {
		// TODO Auto-generated constructor stub
	}
	public CategoryPair(Category cat1, Category cat2) {
		this.cat1=cat1;
		this.cat2=cat2;
		id=cat1.getName()+"--"+cat2.getName();
		// TODO Auto-generated constructor stub
	}
	

}
