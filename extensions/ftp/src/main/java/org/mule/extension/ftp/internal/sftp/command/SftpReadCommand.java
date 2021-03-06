/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ftp.internal.sftp.command;

import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.lock.NullPathLock;
import org.mule.extension.file.common.api.lock.PathLock;
import org.mule.extension.ftp.api.FtpFileAttributes;
import org.mule.extension.ftp.internal.FtpConnector;
import org.mule.extension.ftp.internal.sftp.SftpInputStream;
import org.mule.extension.ftp.internal.sftp.connection.SftpClient;
import org.mule.extension.ftp.internal.sftp.connection.SftpFileSystem;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link SftpCommand} which implements the {@link ReadCommand} contract
 *
 * @since 4.0
 */
public final class SftpReadCommand extends SftpCommand implements ReadCommand {

  /**
   * {@inheritDoc}
   */
  public SftpReadCommand(SftpFileSystem fileSystem, SftpClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result<InputStream, FileAttributes> read(FileConnectorConfig config, String filePath, MediaType mediaType,
                                                  boolean lock) {
    FtpFileAttributes attributes = getExistingFile(filePath);
    if (attributes.isDirectory()) {
      throw cannotReadDirectoryException(Paths.get(attributes.getPath()));
    }

    Path path = Paths.get(attributes.getPath());

    PathLock pathLock;
    if (lock) {
      pathLock = fileSystem.lock(path);
    } else {
      fileSystem.verifyNotLocked(path);
      pathLock = new NullPathLock();
    }

    try {
      InputStream payload = SftpInputStream.newInstance((FtpConnector) config, attributes, pathLock);
      MediaType resolvedMediaType = fileSystem.getFileMessageMediaType(mediaType, attributes);
      return Result.<InputStream, FileAttributes>builder().output(payload).mediaType(resolvedMediaType).attributes(attributes)
          .build();
    } catch (ConnectionException e) {
      throw exception("Could not obtain connection to fetch file " + path, e);
    }
  }
}
