/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.extension.api.runtime.parameter.Literal;

public class StaticLiteralValueResolver<T> implements ValueResolver<Literal<T>> {

  private final Literal<T> literal;

  public StaticLiteralValueResolver(String value, Class<T> type) {
    literal = new ImmutableLiteral<T>(value, type);
  }


  @Override
  public Literal<T> resolve(Event event) throws MuleException {
    return literal;
  }

  @Override
  public boolean isDynamic() {
    return false;
  }
}
