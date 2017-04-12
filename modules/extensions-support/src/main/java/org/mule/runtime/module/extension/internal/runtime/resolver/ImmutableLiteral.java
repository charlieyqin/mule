/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.extension.api.runtime.parameter.Literal;

public final class ImmutableLiteral<T> implements Literal<T> {

  private final String value;
  private final Class<T> type;

  public ImmutableLiteral(String value, Class<T> type) {
    this.value = value;
    this.type = type;
  }

  @Override
  public String getLiteralValue() {
    return value;
  }

  @Override
  public Class<T> getType() {
    return type;
  }
}
