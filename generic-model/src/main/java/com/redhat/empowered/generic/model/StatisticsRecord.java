package com.redhat.empowered.generic.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.NormalDistribution;

public class StatisticsRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String key;
	private Integer count=0;
	private BigDecimal avg;
	private BigDecimal stdev;
	private Map<BigDecimal,Integer> frequencyData = new TreeMap<BigDecimal,Integer>();
	private BigDecimal min;
	private BigDecimal max;
	
	private BigDecimal expectedPercentile = new BigDecimal(95);
	private BigDecimal expectedSlaValue = new BigDecimal(30);
	
	private BigDecimal actualPercentile;
	private BigDecimal actualSlaValue;

	public List<Map<String,BigDecimal>> getfrequencyDataAsList(){
		
		List<Map<String,BigDecimal>> frequencyList = new ArrayList<Map<String,BigDecimal>>();
		
		
		for (Entry<BigDecimal, Integer> entry : frequencyData.entrySet()) {
			Map<String,BigDecimal> m = new HashMap<String,BigDecimal>();
			m.put("x",entry.getKey());
			m.put("y",new BigDecimal(entry.getValue()));
			frequencyList.add(m);
		}
		
		return frequencyList;
		
	}
	
	public BigDecimal getExpectedPercentile() {
		return expectedPercentile;
	}

	public void setExpectedPercentile(BigDecimal expectedPercentile) {
		this.expectedPercentile = expectedPercentile;
	}

	public BigDecimal getExpectedSlaValue() {
		return expectedSlaValue;
	}

	public void setExpectedSlaValue(BigDecimal expectedSlaValue) {
		this.expectedSlaValue = expectedSlaValue;
	}

	public BigDecimal getActualPercentile() {
		
		NormalDistribution normalDistribution = new NormalDistribution(this.avg.doubleValue(),Math.sqrt(this.stdev.doubleValue()));
		actualPercentile = new BigDecimal(normalDistribution.cumulativeProbability(expectedSlaValue.doubleValue()));
		actualPercentile = actualPercentile.multiply(BigDecimal.valueOf(100));
		return actualPercentile;
	}

	public void setActualPercentile(BigDecimal actualPercentile) {
		
		this.actualPercentile = actualPercentile;
	}

	public BigDecimal getActualSlaValue() {
		NormalDistribution normalDistribution = new NormalDistribution(this.avg.doubleValue(),Math.sqrt(this.stdev.doubleValue()));
		actualSlaValue = new BigDecimal(normalDistribution.inverseCumulativeProbability(expectedPercentile.divide(BigDecimal.valueOf(100)).doubleValue()));
		return actualSlaValue;
	}

	public void setActualSlaValue(BigDecimal actualSlaValue) {
		this.actualSlaValue = actualSlaValue;
	}

	public Map<BigDecimal, Integer> getFrequencyData() {
		return frequencyData;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public void setFrequencyData(Map<BigDecimal, Integer> frequencyData) {
		this.frequencyData = frequencyData;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public BigDecimal getAvg() {
		return avg;
	}
	public void setAvg(BigDecimal avg) {
		this.avg = avg;
	}
	public BigDecimal getStdev() {
		return stdev;
	}
	public void setStdev(BigDecimal stdev) {
		this.stdev = stdev;
	}

	public void update(IndicatorRecord indicatorRecord){

		
		//calculate average & standard deviation
		BigDecimal newAvg;
		BigDecimal newStdev = new BigDecimal(0);
		Integer newCount = this.count + 1;
		BigDecimal bigDecimalcount = new BigDecimal(this.count);
		BigDecimal bigDecimalNewCount = new BigDecimal(newCount);
		
		//calculate average		
		if (this.count == 0) {
			newAvg=indicatorRecord.getValue();
			this.min = indicatorRecord.getValue();
			this.max = indicatorRecord.getValue();
		}else{
			newAvg = ((this.avg.multiply(bigDecimalcount)).add(indicatorRecord.getValue())).divide(bigDecimalNewCount,MathContext.DECIMAL64);
		}
		// calculate standard deviation		
		if (this.count >= 1) {
//			newStdev = bigDecimalNewCount.subtract(BigDecimal.valueOf(2)).divide
//					(
//							bigDecimalNewCount.subtract(BigDecimal.valueOf(1))
//							,MathContext.DECIMAL64).multiply(this.stdev);
//			newStdev = newStdev.add(indicatorRecord.getValue().subtract(this.avg).pow(2)).divide(bigDecimalNewCount,MathContext.DECIMAL64);
			
			BigDecimal n_minus_2 = bigDecimalNewCount.subtract(BigDecimal.valueOf(2));
			BigDecimal n_minus_2_sn1 = n_minus_2.multiply(this.stdev);
			BigDecimal n_minus_1 =  bigDecimalNewCount.subtract(BigDecimal.valueOf(1));
			BigDecimal xn1_minus_xn_square = this.avg.subtract(newAvg).pow(2);
			BigDecimal xn_minus_xn_square = indicatorRecord.getValue().subtract(newAvg).pow(2);
			
			newStdev = n_minus_2_sn1.add(n_minus_1.multiply(xn1_minus_xn_square)).add(xn_minus_xn_square).divide(n_minus_1,MathContext.DECIMAL64);
			
			
		}
		
		
		//calculate min/max
		if (this.max.compareTo(indicatorRecord.getValue()) < 0 ){
			this.max = indicatorRecord.getValue();
		}
		if (this.min.compareTo(indicatorRecord.getValue()) > 0 ){
			this.min = indicatorRecord.getValue();
		}
		
		this.count = newCount;
		this.avg = newAvg;
		this.stdev = newStdev;

		//calculate percentiles and slas
		
		//calculate frequency chart
		Integer currentFrequency = frequencyData.get(indicatorRecord.frequencyGroupValue());
		if (currentFrequency!=null){
			frequencyData.put(indicatorRecord.frequencyGroupValue(), currentFrequency+1);
		}else
		{
			frequencyData.put(indicatorRecord.frequencyGroupValue(), 1);
		}
	}


}
