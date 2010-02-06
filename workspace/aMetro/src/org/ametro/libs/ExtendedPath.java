package org.ametro.libs;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class ExtendedPath extends Path {

	private final float SMOOTH_INTERNAL_VALUE = 1.0f;
	private final float SMOOTH_EXTERNAL_VALUE = 0.5f;

	public void lineToClipped(Rect r, Point p0, Point p1, boolean clipInner){
		boolean isInternal = Algorithms.clipCohenSutherland(r, p0, p1);
		if(clipInner && isInternal || !clipInner && !isInternal){
			lineTo(p1.x, p1.y);
		}else{
			moveTo(p1.x,p1.y);
		}
	}

	public void drawSplineClipped(Rect r, Point[] points, int begin, int count, boolean clipInner) {
		Point p0 = points[begin];
		moveTo(p0.x, p0.y);
		if(count == 2){
			Point p1 = points[begin+1];
			boolean isInternal = Algorithms.clipCohenSutherland(r, p0, p1);
			if(clipInner && isInternal || !clipInner && !isInternal){
				lineTo(p1.x, p1.y);
			}else{
				moveTo(p1.x,p1.y);
			}
		}else if(count == 3){
			Point p1 = points[begin+1];
			Point p2 = points[begin+2];
			boolean isInternal = Algorithms.clipCohenSutherland(r, p0, p1) || Algorithms.clipCohenSutherland(r, p1, p2); 
			if(clipInner && isInternal || !clipInner && !isInternal){
				PointF c = Algorithms.interpolateQuadBezier(p0, p1, p2);
				quadTo(c.x, c.y, p2.x, p2.y);
			}else{
				moveTo(p1.x,p1.y);
			}

		}
		else if(count == 4){
			Point p1 = points[begin+1];
			Point p2 = points[begin+2];
			Point p3 = points[begin+3];
			boolean isInternal = Algorithms.clipCohenSutherland(r, p0, p1) || Algorithms.clipCohenSutherland(r, p1, p2); 
			if(clipInner && isInternal || !clipInner && !isInternal){
				QBezierControls ctrl = Algorithms.interpolateCubicBezierControl(p0, p1, p2, p3);
				cubicTo(ctrl.x0, ctrl.y0, ctrl.x1, ctrl.y1, p3.x, p3.y );
			}else{
				moveTo(p3.x,p3.y);
			}
		}else{
			int idx = 1;
			while( (idx+2) < count ){
				Point p1 = points[begin+idx];
				Point p2 = points[begin+idx+1];
				Point p3 = points[begin+idx+2];
				if(idx == 1){ // need to draw first part of spline
					boolean clip01 = Algorithms.clipCohenSutherland(r, p0, p1);
					if(clipInner && clip01 || !clipInner && !clip01){
						QBezierControls ctrlExt = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_EXTERNAL_VALUE);
						float cx = p1.x - (ctrlExt.x0 - p1.x);
						float cy = p1.y - (ctrlExt.y0 - p1.y);
						quadTo(cx, cy, p1.x,p1.y);
					}else{
						moveTo(p1.x, p1.y);
					}
				}
				boolean clip12 = Algorithms.clipCohenSutherland(r, p1, p2); 
				if(clipInner && clip12 || !clipInner && !clip12){
					QBezierControls ctrl = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_INTERNAL_VALUE);
					cubicTo(ctrl.x0, ctrl.y0, ctrl.x1, ctrl.y1, p2.x, p2.y );
				}else{
					moveTo(p2.x, p2.y);
				}
				if(idx+3 == count){
					boolean clip23 = Algorithms.clipCohenSutherland(r, p2, p3);
					if(clipInner && clip23 || !clipInner && !clip23){
						QBezierControls ctrlExt = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_EXTERNAL_VALUE);
						float cx = p2.x - (ctrlExt.x1 - p2.x);
						float cy = p2.y - (ctrlExt.y1 - p2.y);
						quadTo(cx, cy, p3.x,p3.y);
					}else{
						moveTo(p3.x,p3.y);
					}
				}
				p0 = p1;
				idx++;
			}
		}
	}	

	public void drawSpline(Point[] points, int begin, int count) {
		Point p0 = points[begin];
		moveTo(p0.x, p0.y);
		if(count == 2){
			Point p1 = points[begin+1];
			lineTo(p1.x, p1.y);
		}else if(count == 3){
			Point p1 = points[begin+1];
			Point p2 = points[begin+2];
			PointF c = Algorithms.interpolateQuadBezier(p0, p1, p2);
			quadTo(c.x, c.y, p2.x, p2.y);
		}
		else if(count == 4){
			Point p1 = points[begin+1];
			Point p2 = points[begin+2];
			Point p3 = points[begin+3];
			QBezierControls ctrl = Algorithms.interpolateCubicBezierControl(p0, p1, p2, p3);
			cubicTo(ctrl.x0, ctrl.y0, ctrl.x1, ctrl.y1, p3.x, p3.y );
		}else{
			int idx = 1;
			while( (idx+2) < count ){
				Point p1 = points[begin+idx];
				Point p2 = points[begin+idx+1];
				Point p3 = points[begin+idx+2];
				QBezierControls ctrl = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_INTERNAL_VALUE);
				if(idx == 1){
					QBezierControls ctrlExt = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_EXTERNAL_VALUE);
					float cx = p1.x - (ctrlExt.x0 - p1.x);
					float cy = p1.y - (ctrlExt.y0 - p1.y);
					quadTo(cx, cy, p1.x,p1.y);
				}
				cubicTo(ctrl.x0, ctrl.y0, ctrl.x1, ctrl.y1, p2.x, p2.y );
				if(idx+3 == count){
					QBezierControls ctrlExt = Algorithms.interpolateCubeBezierSmooth(p0, p1, p2, p3, SMOOTH_EXTERNAL_VALUE);
					float cx = p2.x - (ctrlExt.x1 - p2.x);
					float cy = p2.y - (ctrlExt.y1 - p2.y);
					quadTo(cx, cy, p3.x,p3.y);
				}
				p0 = p1;
				idx++;
			}
		}
	}	

	public void drawCircleArc(float x1, float y1, float x2, float y2, float x3, float y3) {
		ExtendedGraphic2D.drawCircleArc(this,x1,y1,x2,y2,x3,y3);
	}


}