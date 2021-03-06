/*
 * Copyright (c) 2012 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.linkedin.glu.utils.exceptions;

import java.util.Collection;

/**
 * @author yan@pongasoft.com
 */
public class MultipleExceptions extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  private final Collection<? extends Throwable> _causes;

  public class More extends RuntimeException
  {
    private static final long serialVersionUID = 1L;

    public More(String message)
    {
      super(message);
    }

    public More(int exceptionIndex, Throwable throwable)
    {
      super(computeMessage(exceptionIndex, _causes.size(), throwable), throwable);
      setStackTrace(new StackTraceElement[0]);
    }

    Throwable findRootCause()
    {
      Throwable rootCause = getCause();
      while(rootCause.getCause() != null)
        rootCause = rootCause.getCause();
      return rootCause;
    }
  }

  private static String computeMessage(int exceptionIndex, int totalCount, Throwable more)
  {
    StringBuilder sb = new StringBuilder();

    sb.append("...[").append(exceptionIndex).append("/").append(totalCount).append("]");
    if(more.getMessage() != null)
      sb.append(" ").append(more.getMessage());

    return sb.toString();
  }

  private static String computeMessage(String message, Collection<? extends Throwable> causes)
  {
    StringBuilder sb = new StringBuilder();

    sb.append(message);

    sb.append(" - Multi[").append(causes.size()).append("]...");

    return sb.toString();
  }

  /**
   * So that it can be rebuilt...
   */
  public MultipleExceptions(String message)
  {
    super(message);
    _causes = null;
  }

  /**
   * Constructor
   */
  private MultipleExceptions(String message, Collection<? extends Throwable> causes)
  {
    super(computeMessage(message, causes));

    _causes = causes;

    Throwable th = this;
    int exceptionIndex = 1;

    for(Throwable cause : _causes)
    {
      More more = new More(exceptionIndex++, cause);
      th.initCause(more);
      th = more.findRootCause();
    }
  }

  public Collection<? extends Throwable> getCauses()
  {
    return _causes;
  }

  /**
   * Convenient call to throw a multiple exception or not depending on the collection
   */
  public static void throwIfExceptions(Collection<? extends Throwable> causes) throws Throwable
  {
    throwIfExceptions((String) null, causes);
  }

  /**
   * Convenient call to throw a multiple exception or not depending on the collection
   */
  public static void throwIfExceptions(String message,
                                       Collection<? extends Throwable> causes) throws Throwable
  {
    Throwable throwable = createIfExceptions(message, causes);
    if(throwable != null)
      throw throwable;
  }

  /**
   * Convenient call to throw a multiple exception or not depending on the collection
   *
   * @param rootCause the exception that will be thrown in the end (if causes) and
   *                  attach the multi exception as the cause
   */
  public static void throwIfExceptions(Throwable rootCause,
                                       Collection<? extends Throwable> causes) throws Throwable
  {
    Throwable throwable = createIfExceptions(rootCause, causes);
    if(throwable != null)
      throw throwable;
  }

  /**
   * Convenient call to create a multiple exception or not depending on the collection
   */
  public static Throwable createIfExceptions(Collection<? extends Throwable> causes)
  {
    return createIfExceptions((String) null, causes);
  }

  /**
   * Convenient call to create a multiple exception or not depending on the collection
   */
  public static Throwable createIfExceptions(String message,
                                             Collection<? extends Throwable> causes)
  {
    if(causes == null)
      return null;

    if(causes.isEmpty())
      return null;

    if(causes.size() == 1)
      return causes.iterator().next();

    return new MultipleExceptions(message, causes);
  }

  /**
   * Convenient call to create a multiple exception or not depending on the collection
   *
   * @param rootCause the exception that will be returned in the end (if causes) and
   *                  attach the multi exception as the cause
   */
  public static <T extends Throwable> T createIfExceptions(T rootCause,
                                                           Collection<? extends Throwable> causes)
  {
    Throwable throwable = createIfExceptions(rootCause.getMessage(), causes);
    if(throwable != null)
    {
      rootCause.initCause(throwable);
      return rootCause;
    }
    else
      return null;
  }
}
