package de.ddkfm.util;

import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream {

	private StringBuilder mBuf;

	public String getString() {
		return mBuf.toString();
	}

	@Override
	public void write(int b) throws IOException {
		mBuf.append((char) b);
	}
}