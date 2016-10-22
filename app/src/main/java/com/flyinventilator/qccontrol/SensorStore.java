package com.flyinventilator.qccontrol;

import java.util.LinkedList;

public class SensorStore {
	private LinkedList<double[][]> Raws = new LinkedList<double[][]>();
	
	private double Temperature = 0.0;
	
	private double LiPoState = 0.0;
	
	//Getter
	public LinkedList<double[][]> getRaws(){
		return Raws;
	}
	
	public double getTemperature(){
		return Temperature;
	}
	
	public double getLiPoState(){
		return LiPoState;
	}
	
	//Setter
	public void setRaws(double[][] rawMatrix){
		Raws.push(rawMatrix);
		while (Raws.size()>5){
			Raws.remove(5);
		}
	}
	
	public void setTemperature(double value){
		this.Temperature = value;
	}
	
	public void setLiPoState(double value){
		this.LiPoState =  value;
	}
	
}
