/*
 * Copyright (C) 2008, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2006, Shawn O. Pearce <spearce@spearce.org>
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

package org.spearce.jgit.lib;

import java.io.IOException;

public class T0008_testparserev extends RepositoryTestCase {

	public void testObjectId_existing() throws IOException {
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0").toString());
	}

	public void testObjectId_nonexisting() throws IOException {
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c1",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c1").toString());
	}

	public void testObjectId_objectid_implicit_firstparent() throws IOException {
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^").toString());
		assertEquals("1203b03dc816ccbb67773f28b3c19318654b0bc8",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^^").toString());
		assertEquals("bab66b48f836ed950c99134ef666436fb07a09a0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^^^").toString());
	}

	public void testObjectId_objectid_self() throws IOException {
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^0").toString());
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^0^0").toString());
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^0^0^0").toString());
	}

	public void testObjectId_objectid_explicit_firstparent() throws IOException {
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^1").toString());
		assertEquals("1203b03dc816ccbb67773f28b3c19318654b0bc8",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^1^1").toString());
		assertEquals("bab66b48f836ed950c99134ef666436fb07a09a0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^1^1^1").toString());
	}

	public void testObjectId_objectid_explicit_otherparents() throws IOException {
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^1").toString());
		assertEquals("f73b95671f326616d66b2afb3bdfcdbbce110b44",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^2").toString());
		assertEquals("d0114ab8ac326bab30e3a657a0397578c5a1af88",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^3").toString());
		assertEquals("d0114ab8ac326bab30e3a657a0397578c5a1af88",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^03").toString());
	}

	public void testRef_refname() throws IOException {
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("master^0").toString());
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("master^").toString());
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("refs/heads/master^1").toString());
	}

	public void testDistance() throws IOException {
		assertEquals("6e1475206e57110fcef4b92320436c1e9872a322",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0~0").toString());
		assertEquals("1203b03dc816ccbb67773f28b3c19318654b0bc8",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0~1").toString());
		assertEquals("bab66b48f836ed950c99134ef666436fb07a09a0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0~2").toString());
		assertEquals("bab66b48f836ed950c99134ef666436fb07a09a0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0~02").toString());
	}

	public void testTree() throws IOException {
		assertEquals("6020a3b8d5d636e549ccbd0c53e2764684bb3125",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^{tree}").toString());
		assertEquals("02ba32d3649e510002c21651936b7077aa75ffa9",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^^{tree}").toString());
	}

	public void testHEAD() throws IOException {
		assertEquals("6020a3b8d5d636e549ccbd0c53e2764684bb3125",db.resolve("HEAD^{tree}").toString());
	}

	public void testDerefCommit() throws IOException {
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^{}").toString());
		assertEquals("49322bb17d3acc9146f98c97d078513228bbf3c0",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^{commit}").toString());
		// double deref
		assertEquals("6020a3b8d5d636e549ccbd0c53e2764684bb3125",db.resolve("49322bb17d3acc9146f98c97d078513228bbf3c0^{commit}^{tree}").toString());
	}

	public void testDerefTag() throws IOException {
		assertEquals("fd608fbe625a2b456d9f15c2b1dc41f252057dd7",db.resolve("spearce-gpg-pub^{}").toString());
		assertEquals("fd608fbe625a2b456d9f15c2b1dc41f252057dd7",db.resolve("spearce-gpg-pub^{blob}").toString());
	}

	public void testDerefBlob() throws IOException {
		assertEquals("fd608fbe625a2b456d9f15c2b1dc41f252057dd7",db.resolve("fd608fbe625a2b456d9f15c2b1dc41f252057dd7^{}").toString());
		assertEquals("fd608fbe625a2b456d9f15c2b1dc41f252057dd7",db.resolve("fd608fbe625a2b456d9f15c2b1dc41f252057dd7^{blob}").toString());
	}
	
	// TODO: ^{tree} for a tag pointing to a tag
}
