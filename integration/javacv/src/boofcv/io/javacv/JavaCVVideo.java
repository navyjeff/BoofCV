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

package boofcv.io.javacv;

import boofcv.io.image.SimpleImageSequence;
import boofcv.io.video.VideoInterface;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * @author Peter Abeles
 */
public class JavaCVVideo implements VideoInterface {
	@Override
	public <T extends ImageBase> SimpleImageSequence<T>
	load(String fileName, ImageType<T> imageType) {
		return new JavaCVVideoImageSequence<T>(fileName,imageType);
	}
}
