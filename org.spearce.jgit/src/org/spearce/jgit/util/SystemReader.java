/*
 * Copyright (C) 2009, Yann Simon <yann.simon.fr@gmail.com>
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

package org.spearce.jgit.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.spearce.jgit.lib.RepositoryConfig;

/**
 * Interface to read values from the system.
 * <p>
 * When writing unit tests, extending this interface with a custom class
 * permits to simulate an access to a system variable or property and
 * permits to control the user's global configuration.
 * </p>
 */
public abstract class SystemReader {
	private static SystemReader INSTANCE = new SystemReader() {
		private volatile String hostname;

		public String getenv(String variable) {
			return System.getenv(variable);
		}

		public String getProperty(String key) {
			return System.getProperty(key);
		}

		public RepositoryConfig openUserConfig() {
			final File home = FS.userHome();
			return new RepositoryConfig(null, new File(home, ".gitconfig"));
		}

		public String getHostname() {
			if (hostname == null) {
				try {
					InetAddress localMachine = InetAddress.getLocalHost();
					hostname = localMachine.getCanonicalHostName();
				} catch (UnknownHostException e) {
					// we do nothing
					hostname = "localhost";
				}
				assert hostname != null;
			}
			return hostname;
		}
	};

	/** @return the live instance to read system properties. */
	public static SystemReader getInstance() {
		return INSTANCE;
	}

	/**
	 * @param newReader
	 *            the new instance to use when accessing properties.
	 */
	public static void setInstance(SystemReader newReader) {
		INSTANCE = newReader;
	}

	/**
	 * Gets the hostname of the local host. If no hostname can be found, the
	 * hostname is set to the default value "localhost".
	 *
	 * @return the canonical hostname
	 */
	public abstract String getHostname();

	/**
	 * @param variable system variable to read
	 * @return value of the system variable
	 */
	public abstract String getenv(String variable);

	/**
	 * @param key of the system property to read
	 * @return value of the system property
	 */
	public abstract String getProperty(String key);

	/**
	 * @return the git configuration found in the user home
	 */
	public abstract RepositoryConfig openUserConfig();
}
