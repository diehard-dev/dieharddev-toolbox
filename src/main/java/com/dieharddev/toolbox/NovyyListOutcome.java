package com.dieharddev.toolbox;

/**

* Copyright (c) 2012-2018, jcabi.com

* All rights reserved.

*

* Redistribution and use in source and binary forms, with or without

* modification, are permitted provided that the following conditions

* are met: 1) Redistributions of source code must retain the above

* copyright notice, this list of conditions and the following

* disclaimer. 2) Redistributions in binary form must reproduce the above

* copyright notice, this list of conditions and the following

* disclaimer in the documentation and/or other materials provided

* with the distribution. 3) Neither the name of the jcabi.com nor

* the names of its contributors may be used to endorse or promote

* products derived from this software without specific prior written

* permission.

*

* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS

* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT

* NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND

* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL

* THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,

* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES

* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR

* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)

* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,

* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)

* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED

* OF THE POSSIBILITY OF SUCH DAMAGE.

*/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.jcabi.jdbc.Outcome;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * Outcome that returns a list.
 *
 * 
 * 
 * <p>
 * 
 * Use it when you need a full collection:
 *
 * 
 * 
 * <pre>

*  Collection&lgt;User&gt; users = new JdbcSession(source)

*   .sql("SELECT * FROM user")

*   .select(

*     new ListOutcome&lt;User&gt;(

*       new ListOutcome.Mapping&lt;User&gt;() {

*         &#64;Override

*         public User map(final ResultSet rset) throws SQLException {

*           return new User.Simple(rset.getLong(1), rset.getString(2));

*         }

*       }

*     )

*   );
 * 
 * </pre>
 *
 * 
 * 
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * 
 * @version $Id: 594ecd93d1eacae0e815614e79db46068b52c380 $
 * 
 * @since 0.13
 * 
 * @param <T> Type of items
 * 
 */
@ToString
@EqualsAndHashCode(of = "mapping")
public final class NovyyListOutcome<T> implements Outcome<List<T>> {
	/**
	 * 
	 * Mapping.
	 * 
	 */
	private final transient NovyyListOutcome.Mapping<T> mapping;
	private final int fetchSize;

	/**
	 * 
	 * Public ctor.
	 *
	 * 
	 * 
	 * @param fetchSize
	 *
	 * 
	 * 
	 * @param mpg       Mapping
	 * 
	 */
	public NovyyListOutcome(int fetchSize, final NovyyListOutcome.Mapping<T> mpg) {
		this.mapping = mpg;
		this.fetchSize = fetchSize;
	}

	@Override
	public List<T> handle(final ResultSet resultSet, final Statement stmt) throws SQLException {
		final List<T> result = new LinkedList<>();
		boolean hasNext = resultSet.next();
		if (hasNext == true) {
			resultSet.setFetchSize(fetchSize);
		}
		while (hasNext) {
			result.add(this.mapping.map(resultSet));
			hasNext = resultSet.next();
		}
		return result;
	}

	/**
	 * 
	 * Mapping.
	 *
	 * 
	 * 
	 * @param <T> Type of output
	 * 
	 */
	public interface Mapping<T> {
		/**
		 * 
		 * Map.
		 *
		 * 
		 * 
		 * @param rset Result set
		 * 
		 * @return Object
		 * 
		 * @throws SQLException If fails
		 * 
		 */
		T map(ResultSet rset) throws SQLException;
	}
}
