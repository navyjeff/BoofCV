/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.fiducial.square;

import boofcv.abst.filter.binary.InputToBinary;
import boofcv.alg.geo.PerspectiveOps;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.alg.shapes.polygon.BinaryPolygonDetector;
import boofcv.core.image.ConvertImage;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.factory.shape.ConfigPolygonDetector;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.struct.calib.IntrinsicParameters;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.transform.se.SePointOps_F64;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestDetectFiducialSquareBinary {

	static int gridWidth = 4;
	static double borderWidth = 0.25;
	static double blackBorderFraction = 0.65;

	Random rand = new Random(234);
	BinaryPolygonDetector<GrayU8> squareDetector = FactoryShapeDetector.polygon(
			new ConfigPolygonDetector(false, 4,4),GrayU8.class);
	InputToBinary<GrayU8> inputToBinary = FactoryThresholdBinary.globalFixed(50, true, GrayU8.class);

	/**
	 * Makes sure the found rotation matrix is correct
	 */
	@Test
	public void checkFoundRotationMatrix() {

		IntrinsicParameters intrinsic = new IntrinsicParameters(500,500,0,320,240,640,480);

		GrayF32 rendered_F32 = create(DetectFiducialSquareBinary.w, 314);
		GrayU8 rendered = new GrayU8(rendered_F32.width,rendered_F32.height);
		ConvertImage.convert(rendered_F32,rendered);
		GrayU8 input = new GrayU8(640,480);

		List<Point2D_F64> expected = new ArrayList<Point2D_F64>();
		expected.add( new Point2D_F64(200,250+rendered.height));
		expected.add( new Point2D_F64(200,250));
		expected.add( new Point2D_F64(200+rendered.width,250));
		expected.add( new Point2D_F64(200+rendered.width,250+rendered.height));

		for (int i = 0; i < 4; i++) {
			ImageMiscOps.fill(input, 255);
			input.subimage(200, 250, 200 + rendered.width, 250 + rendered.height, null).setTo(rendered);

			DetectFiducialSquareBinary<GrayU8> alg =
					new DetectFiducialSquareBinary<GrayU8>(gridWidth,borderWidth,blackBorderFraction,
							inputToBinary,squareDetector, GrayU8.class);
			alg.setLengthSide(2);
			alg.configure(intrinsic, false);
			alg.process(input);

			assertEquals(1, alg.getFound().size());
			FoundFiducial ff = alg.getFound().get(0);

			// lower left hand corner in the fiducial.  side is of length 2
			Point3D_F64 lowerLeft = new Point3D_F64(-1, -1, 0);
			Point3D_F64 cameraPt = new Point3D_F64();
			SePointOps_F64.transform(ff.targetToSensor, lowerLeft, cameraPt);
			Point2D_F64 pixelPt = new Point2D_F64();
			PerspectiveOps.convertNormToPixel(intrinsic, cameraPt.x / cameraPt.z, cameraPt.y / cameraPt.z, pixelPt);

//			System.out.println(pixelPt);
			// see if that point projects into the correct location
			assertEquals(expected.get(i).x, pixelPt.x, 1e-4);
			assertEquals(expected.get(i).y, pixelPt.y, 1e-4);

			ImageMiscOps.rotateCW(rendered);
		}
	}

	/**
	 * Give it easy positive examples
	 */
	@Test
	public void processSquare() {
		for (int i = 0; i < 4; i++) {
			GrayF32 input = create(DetectFiducialSquareBinary.w, 314);

			for (int j = 0; j < i - 1; j++) {
				ImageMiscOps.rotateCCW(input.clone(), input);
			}
			DetectFiducialSquareBinary alg =
					new DetectFiducialSquareBinary<GrayU8>(gridWidth,borderWidth,blackBorderFraction,
							inputToBinary,squareDetector,GrayU8.class);

			BaseDetectFiducialSquare.Result result = new BaseDetectFiducialSquare.Result();
			assertTrue(alg.processSquare(input, result,0,0));

			assertEquals(314, result.which);
			assertEquals(Math.max(0,i-1), result.rotation);
		}
	}

	/**
	 * Give it random noise.  It should fail
	 */
	@Test
	public void processSquare_negative() {
		GrayF32 input = create(DetectFiducialSquareBinary.w, 314);
		ImageMiscOps.fillUniform(input,rand,0,255);

		DetectFiducialSquareBinary alg =
				new DetectFiducialSquareBinary<GrayU8>(gridWidth,borderWidth,blackBorderFraction,
						inputToBinary,squareDetector,GrayU8.class);

		BaseDetectFiducialSquare.Result result = new BaseDetectFiducialSquare.Result();
		assertFalse(alg.processSquare(input, result,0,0));
	}

	@Test
	public void getNumberOfDistinctFiducials() {
		DetectFiducialSquareBinary alg =
				new DetectFiducialSquareBinary<GrayU8>(3,borderWidth,blackBorderFraction,
						inputToBinary,squareDetector,GrayU8.class);
		assertEquals(32,alg.getNumberOfDistinctFiducials());

		alg = new DetectFiducialSquareBinary<GrayU8>(4,borderWidth,blackBorderFraction,
				inputToBinary,squareDetector,GrayU8.class);
		assertEquals(4096,alg.getNumberOfDistinctFiducials());
		alg = new DetectFiducialSquareBinary<GrayU8>(5,borderWidth,blackBorderFraction,
				inputToBinary,squareDetector,GrayU8.class);
		assertEquals(2097152,alg.getNumberOfDistinctFiducials());
		alg = new DetectFiducialSquareBinary<GrayU8>(6,borderWidth,blackBorderFraction,
				inputToBinary,squareDetector,GrayU8.class);
		assertEquals(4294967296L, alg.getNumberOfDistinctFiducials());
	}

	/**
	 * See if it can detect a 3x3 grid
	 */
	@Test
	public void checkGrid3x3() {
		int number = 9;
		GrayF32 input = create(DetectFiducialSquareBinary.w, number,3, borderWidth);

		DetectFiducialSquareBinary alg =
				new DetectFiducialSquareBinary<GrayU8>(3,borderWidth,blackBorderFraction,
						inputToBinary,squareDetector,GrayU8.class);

		BaseDetectFiducialSquare.Result result = new BaseDetectFiducialSquare.Result();
		assertTrue(alg.processSquare(input, result,0,0));

		assertEquals(number, result.which);
	}

	/**
	 * See if it can detect a 3x3 grid
	 */
	@Test
	public void checkGrid5x5() {
		int number = 299382;
		GrayF32 input = create(DetectFiducialSquareBinary.w, number,5, borderWidth);

		DetectFiducialSquareBinary alg =
				new DetectFiducialSquareBinary<GrayU8>(5,borderWidth,blackBorderFraction,
						inputToBinary,squareDetector,GrayU8.class);

		BaseDetectFiducialSquare.Result result = new BaseDetectFiducialSquare.Result();
		assertTrue(alg.processSquare(input, result,0,0));

		assertEquals(number, result.which);
	}

	/**
	 * See if it can process a border that isn't 0.25
	 */
	@Test
	public void differentBorderSizes() {

		int number = 128;
		double borders[] = new double[]{0.1,0.15,0.3};

		for( double border : borders ) {

			GrayF32 input = create(DetectFiducialSquareBinary.w, number,4, border);

			DetectFiducialSquareBinary alg =
					new DetectFiducialSquareBinary<GrayU8>(gridWidth,border,blackBorderFraction,
							inputToBinary,squareDetector,GrayU8.class);

			BaseDetectFiducialSquare.Result result = new BaseDetectFiducialSquare.Result();
			assertTrue(alg.processSquare(input, result,0,0));

			assertEquals(number, result.which);
		}
	}
	public static GrayF32 create(int square, int value ) {
		return create(square,value,gridWidth,borderWidth);
	}

	public static GrayF32 create(int square, int value, int gridWidth , double borderFraction) {

		int width = (int)Math.round((square*gridWidth)/(1-2.0*borderFraction));

		GrayF32 ret = new GrayF32(width,width);

		int s2 = (int)Math.round(ret.width*borderFraction);
		int s5 = s2+square*(gridWidth-1);

		int N = gridWidth*gridWidth-4;
		for (int i = 0; i < N; i++) {
			if( (value& (1<<i)) != 0 )
				continue;

			int where = index(i, gridWidth);
			int x = where%gridWidth;
			int y = gridWidth-1-(where/gridWidth);

			x = s2 + square*x;
			y = s2 + square*y;

			ImageMiscOps.fillRectangle(ret,0xFF,x,y,square,square);
		}
		ImageMiscOps.fillRectangle(ret,0xFF,s2,s2,square,square);
		ImageMiscOps.fillRectangle(ret,0xFF,s5,s5,square,square);
		ImageMiscOps.fillRectangle(ret,0xFF,s5,s2,square,square);

		return ret;
	}

	private static int index( int bit , int gridWidth ) {
		int transitionBit0 = gridWidth-3;
		int transitionBit1 = transitionBit0 + gridWidth*(gridWidth-2);
		int transitionBit2 = transitionBit1 + gridWidth-2;

		if( bit <= transitionBit0 )
			bit++;
		else if( bit <= transitionBit1 )
			bit += 2;
		else if( bit <= transitionBit2 )
			bit += 3;
		else
			throw new RuntimeException("Bit out of range");

		return bit;
	}
}