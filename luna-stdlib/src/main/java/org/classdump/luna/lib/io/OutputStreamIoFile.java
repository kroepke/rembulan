/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.classdump.luna.lib.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import org.classdump.luna.ByteString;
import org.classdump.luna.Table;
import org.classdump.luna.lib.IoFile;

public class OutputStreamIoFile extends IoFile<SeekableOutputStream> {

  public OutputStreamIoFile(OutputStream out, Table metatable) {
    super(metatable, new SeekableOutputStream(Objects.requireNonNull(out)));
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException("cannot close standard file");
  }

  @Override
  public void flush() throws IOException {
    outputStream().flush();
  }

  @Override
  public void write(ByteString s) throws IOException {
    s.writeTo(outputStream());
  }

  @Override
  public long seek(IoFile.Whence whence, long offset) throws IOException {
    switch (whence) {
      case BEGINNING:
      case END:
        return outputStream().setPosition(offset);

      case CURRENT_POSITION:
        return outputStream().addPosition(offset);

      default:
        throw new IllegalArgumentException("Illegal whence: " + whence);
    }
  }

  private SeekableOutputStream outputStream() {
    return getUserValue();
  }

}
