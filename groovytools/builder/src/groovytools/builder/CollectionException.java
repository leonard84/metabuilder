/*
 *      Copyright 2008 the original author or authors
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

package groovytools.builder;

/**
 * Thrown when an exception occurs building a collection or adding to a collection. 
 *
 * @author didge
 * @version $Id$
 */
public class CollectionException extends MetaBuilderException {
    public CollectionException() {
    }

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionException(Throwable cause) {
        super(cause);
    }
}