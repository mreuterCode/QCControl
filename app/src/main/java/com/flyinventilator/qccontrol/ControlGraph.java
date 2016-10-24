package com.flyinventilator.qccontrol;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SeekBar;

public class ControlGraph extends ImageView {
	Paint middleCircle = new Paint();
	//Paint calibrationCross = new Paint();
	Paint lines = new Paint();
	Paint text = new Paint();
	
	LinkedList logPitch = new LinkedList();
	LinkedList logRoll = new LinkedList();
	
	float pitch = 0.0f;
	float roll = 0.0f;

	/*float calibration_x = 0.0f;
	float calibration_y = 0.0f;*/

	boolean initialized = false;
	
	public ControlGraph(Context context) {
		super(context);
		
		middleCircle.setColor(Color.GREEN);
		middleCircle.setStyle(Paint.Style.STROKE);
		middleCircle.setStrokeWidth(6);

		/*calibrationCross.setColor(Color.RED);
		calibrationCross.setStyle(Paint.Style.STROKE);
		calibrationCross.setStrokeWidth(10);*/

		lines.setColor(Color.BLUE);
		lines.setStrokeWidth(4);
		
		text.setColor(Color.BLACK);
		text.setTextSize(30);
		
		initialized = true;
		
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(initialized){
			canvas.drawLine(0, getHeight()/2, getWidth(), getHeight()/2, lines);
			canvas.drawLine(getWidth()/2, 0, getWidth()/2, getHeight(), lines);
			
			canvas.drawText("Nick", getWidth()/2 + 10, 40, text);
			canvas.drawText("Roll", 10, getHeight()/2 + 40, text);
			


		}
		// Draw cursor circle
		float x_center = getWidth()/2 - pitch*500;
		float y_center = getHeight()/2 - roll*500;

		canvas.drawCircle(x_center, y_center, 70, middleCircle);
		
		canvas.drawLine(x_center - 90, y_center, x_center - 50, y_center, middleCircle);
		canvas.drawLine(x_center + 90, y_center, x_center + 50, y_center, middleCircle);
		
		canvas.drawLine(x_center, y_center - 90, x_center, y_center - 50, middleCircle);
		canvas.drawLine(x_center, y_center + 90, x_center, y_center + 50, middleCircle);

		//Draw calibration cross
		/*float calib_x_center = getWidth()/2 - calibration_x;
		float calib_y_center = getHeight()/2 - calibration_y;

		canvas.drawLine(calib_x_center - 20, calib_y_center, calib_x_center + 20, calib_y_center, calibrationCross);
		canvas.drawLine(calib_x_center, calib_y_center - 20, calib_x_center, calib_y_center + 20, calibrationCross);*/

	}

	public void setPitch(float pitch){
		this.pitch = pitch;
		if(pitch != 0.0f){
			logPitch.add(pitch);
		}
	}
	
	public void setRoll(float roll){
		this.roll = roll;
		if(roll != 0.0f){
			logRoll.add(roll);
		}
	}

}
