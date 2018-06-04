/* Copyright (c) 2008, Nathan Sweet
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.esotericsoftware.kryo.unsafe;

import static com.esotericsoftware.kryo.unsafe.UnsafeUtil.*;

import java.io.InputStream;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

/** An {@link Input} that reads data from a byte[] using sun.misc.Unsafe. Multi-byte primitive types use native byte order, so the
 * native byte order on different computers which read and write the data must be the same. Variable length encoding is not used
 * for int or long to maximize performance.
 * @author Roman Levenstein <romixlev@gmail.com> */
public class UnsafeInput extends Input {
	/** Creates an uninitialized Input, {@link #setBuffer(byte[])} must be called before the Input is used. */
	public UnsafeInput () {
	}

	/** Creates a new Input for reading from a byte[] buffer.
	 * @param bufferSize The size of the buffer. An exception is thrown if more bytes than this are read and
	 *           {@link #fill(byte[], int, int)} does not supply more bytes. */
	public UnsafeInput (int bufferSize) {
		super(bufferSize);
	}

	/** Creates a new Input for reading from a byte[] buffer.
	 * @param buffer An exception is thrown if more bytes than this are read and {@link #fill(byte[], int, int)} does not supply
	 *           more bytes. */
	public UnsafeInput (byte[] buffer) {
		super(buffer);
	}

	/** Creates a new Input for reading from a byte[] buffer.
	 * @param buffer An exception is thrown if more bytes than this are read and {@link #fill(byte[], int, int)} does not supply
	 *           more bytes. */
	public UnsafeInput (byte[] buffer, int offset, int count) {
		super(buffer, offset, count);
	}

	/** Creates a new Input for reading from an InputStream with a buffer size of 4096. */
	public UnsafeInput (InputStream inputStream) {
		super(inputStream);
	}

	/** Creates a new Input for reading from an InputStream with the specified buffer size. */
	public UnsafeInput (InputStream inputStream, int bufferSize) {
		super(inputStream, bufferSize);
	}

	public int readInt () throws KryoException {
		require(4);
		int result = unsafe.getInt(buffer, byteArrayBaseOffset + position);
		position += 4;
		return result;
	}

	public int readVarInt (boolean optimizePositive) throws KryoException {
		return readInt();
	}

	public boolean canReadVarInt () throws KryoException {
		return limit - position >= 4;
	}

	public float readFloat () throws KryoException {
		require(4);
		float result = unsafe.getFloat(buffer, byteArrayBaseOffset + position);
		position += 4;
		return result;
	}

	public short readShort () throws KryoException {
		require(2);
		short result = unsafe.getShort(buffer, byteArrayBaseOffset + position);
		position += 2;
		return result;
	}

	public long readLong () throws KryoException {
		require(8);
		long result = unsafe.getLong(buffer, byteArrayBaseOffset + position);
		position += 8;
		return result;
	}

	public long readVarLong (boolean optimizePositive) throws KryoException {
		return readLong();
	}

	public boolean canReadVarLong () throws KryoException {
		return limit - position >= 8;
	}

	public double readDouble () throws KryoException {
		require(8);
		double result = unsafe.getDouble(buffer, byteArrayBaseOffset + position);
		position += 8;
		return result;
	}

	public char readChar () throws KryoException {
		require(2);
		char result = unsafe.getChar(buffer, byteArrayBaseOffset + position);
		position += 2;
		return result;
	}

	public int[] readVarInts (int length, boolean optimizePositive) throws KryoException {
		int[] array = new int[length];
		readBytes(array, intArrayBaseOffset, 0, length << 2);
		return array;
	}

	public long[] readVarLongs (int length, boolean optimizePositive) throws KryoException {
		long[] array = new long[length];
		readBytes(array, longArrayBaseOffset, 0, length << 3);
		return array;
	}

	public int[] readInts (int length) throws KryoException {
		int[] array = new int[length];
		readBytes(array, intArrayBaseOffset, 0, length << 2);
		return array;
	}

	public long[] readLongs (int length) throws KryoException {
		long[] array = new long[length];
		readBytes(array, longArrayBaseOffset, 0, length << 3);
		return array;
	}

	public float[] readFloats (int length) throws KryoException {
		float[] array = new float[length];
		readBytes(array, floatArrayBaseOffset, 0, length << 2);
		return array;
	}

	public short[] readShorts (int length) throws KryoException {
		short[] array = new short[length];
		readBytes(array, shortArrayBaseOffset, 0, length << 1);
		return array;
	}

	public char[] readChars (int length) throws KryoException {
		char[] array = new char[length];
		readBytes(array, charArrayBaseOffset, 0, length << 1);
		return array;
	}

	public double[] readDoubles (int length) throws KryoException {
		double[] array = new double[length];
		readBytes(array, doubleArrayBaseOffset, 0, length << 3);
		return array;
	}

	/** Reads bytes to the specified array. */
	public void readBytes (Object toArray, long offset, long count) throws KryoException {
		if (!toArray.getClass().isArray()) throw new KryoException("toArray must be an array.");
		readBytes(toArray, 0, offset, (int)count);
	}

	private void readBytes (Object toArray, long toArrayTypeOffset, long offset, int count) throws KryoException {
		int copyCount = Math.min(limit - position, count);
		while (true) {
			unsafe.copyMemory(buffer, byteArrayBaseOffset + position, toArray, toArrayTypeOffset + offset, copyCount);
			position += copyCount;
			count -= copyCount;
			if (count == 0) break;
			offset += copyCount;
			copyCount = Math.min(count, capacity);
			require(copyCount);
		}
	}
}
