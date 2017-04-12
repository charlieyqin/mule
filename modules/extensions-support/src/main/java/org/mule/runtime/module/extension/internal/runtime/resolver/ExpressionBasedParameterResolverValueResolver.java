/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

/**
 * {@link ValueResolver} implementation for {@link ParameterResolver} that are resolved from an
 * expression
 *
 * @since 4.0
 */
public class ExpressionBasedParameterResolverValueResolver<T> implements ValueResolver<ParameterResolver<T>> {

  private final String exp;
  private final MetadataType metadataType;
  private final MuleContext muleContext;

  public ExpressionBasedParameterResolverValueResolver(String exp, MetadataType metadataType, MuleContext muleContext) {
    this.exp = exp;
    this.metadataType = metadataType;
    this.muleContext = muleContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ParameterResolver<T> resolve(Event event) throws MuleException {
    return new ExpressionBasedParameterResolver<>(exp, metadataType, muleContext, event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return true;
  }
}
