/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
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

package org.spearce.jgit.pgm;

import org.spearce.jgit.lib.AnyObjectId;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.transport.FetchConnection;
import org.spearce.jgit.transport.Transport;

class LsRemote extends TextBuiltin {
	@Override
	void execute(final String[] args) throws Exception {
		int argi = 0;
		for (; argi < args.length; argi++) {
			final String a = args[argi];
			if ("--".equals(a)) {
				argi++;
				break;
			} else
				break;
		}

		if (argi == args.length)
			throw die("usage: url");
		else if (argi + 1 < args.length)
			throw die("too many arguments");

		final Transport tn = Transport.open(db, args[argi]);
		final FetchConnection c = tn.openFetch();
		try {
			for (final Ref r : c.getRefs()) {
				show(r.getObjectId(), r.getName());
				if (r.getPeeledObjectId() != null)
					show(r.getPeeledObjectId(), r.getName() + "^{}");
			}
		} finally {
			c.close();
		}
	}

	private void show(final AnyObjectId id, final String name) {
		out.print(id);
		out.print('\t');
		out.print(name);
		out.println();
	}
}
