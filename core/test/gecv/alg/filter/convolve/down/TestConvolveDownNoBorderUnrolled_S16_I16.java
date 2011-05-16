/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.filter.convolve.down;

import gecv.struct.convolve.Kernel1D_I32;
import gecv.struct.convolve.Kernel2D_I32;
import gecv.struct.image.ImageInt16;
import gecv.struct.image.ImageSInt16;


/**
 * @author Peter Abeles
 */
public class TestConvolveDownNoBorderUnrolled_S16_I16 extends StandardConvolveUnrolledTests {

	public TestConvolveDownNoBorderUnrolled_S16_I16() {
		this.numUnrolled = GenerateConvolvedDownNoBorderUnrolled.numUnrolled;
		this.target = ConvolveDownNoBorderUnrolled_S16_I16.class;
		this.param1D = new Class<?>[]{Kernel1D_I32.class, ImageSInt16.class, ImageInt16.class , int.class };
		this.param2D = new Class<?>[]{Kernel2D_I32.class, ImageSInt16.class, ImageInt16.class , int.class };
	}
}
