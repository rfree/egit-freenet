/*
 * Copyright (C) 2009, Google Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spearce.jgit.revwalk;

import org.spearce.jgit.lib.Constants;

/** Case insensitive key for a {@link FooterLine}. */
public final class FooterKey {
	/** Standard {@code Signed-off-by} */
	public static final FooterKey SIGNED_OFF_BY = new FooterKey("Signed-off-by");

	/** Standard {@code Acked-by} */
	public static final FooterKey ACKED_BY = new FooterKey("Acked-by");

	/** Standard {@code CC} */
	public static final FooterKey CC = new FooterKey("CC");

	private final String name;

	final byte[] raw;

	/**
	 * Create a key for a specific footer line.
	 *
	 * @param keyName
	 *            name of the footer line.
	 */
	public FooterKey(final String keyName) {
		name = keyName;
		raw = Constants.encode(keyName.toLowerCase());
	}

	/** @return name of this footer line. */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "FooterKey[" + name + "]";
	}
}
