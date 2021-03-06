/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.core.api.processor.InterceptingMessageProcessor;
import org.mule.runtime.core.internal.streaming.object.iterator.Consumer;
import org.mule.runtime.core.internal.streaming.object.iterator.ListConsumer;
import org.mule.runtime.core.internal.streaming.object.iterator.Producer;
import org.mule.runtime.core.internal.streaming.object.iterator.ConsumerStreamingIterator;
import org.mule.runtime.core.policy.PolicyManager;
import org.mule.runtime.core.streaming.CursorProviderFactory;
import org.mule.runtime.extension.api.runtime.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.ConfigurationProvider;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.mule.runtime.module.extension.internal.runtime.ExecutionContextAdapter;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.streaming.PagingProviderProducer;

import reactor.core.publisher.Mono;

/**
 * A specialization of {@link OperationMessageProcessor} which also implements {@link InterceptingMessageProcessor}.
 *
 * @since 4.0
 */
public class PagedOperationMessageProcessor extends OperationMessageProcessor {

  public PagedOperationMessageProcessor(ExtensionModel extensionModel,
                                        OperationModel operationModel,
                                        ConfigurationProvider configurationProvider,
                                        String target,
                                        ResolverSet resolverSet,
                                        CursorProviderFactory cursorProviderFactory,
                                        ExtensionManager extensionManager,
                                        PolicyManager policyManager) {
    super(extensionModel, operationModel, configurationProvider, target, resolverSet, cursorProviderFactory,
          extensionManager, policyManager);
  }

  @Override
  protected Mono<Event> doProcess(Event event, ExecutionContextAdapter operationContext) {
    try {
      Event resultEvent = super.doProcess(event, operationContext).block();
      PagingProvider<?, ?> pagingProvider = (PagingProvider) resultEvent.getMessage().getPayload().getValue();

      if (pagingProvider == null) {
        throw new IllegalStateException("Obtained paging delegate cannot be null");
      }

      Producer<?> producer =
          new PagingProviderProducer(pagingProvider, (ConfigurationInstance) operationContext.getConfiguration().get(),
                                     connectionManager);
      Consumer<?> consumer = new ListConsumer(producer);

      return just(returnDelegate.asReturnValue(new ConsumerStreamingIterator<>(consumer), operationContext));
    } catch (Exception e) {
      return error(e);
    }
  }
}
