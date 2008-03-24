/*
 *  Copyright (C) 2008  Shawn Pearce <spearce@spearce.org>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License, version 2, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */
package org.spearce.jgit.revwalk;

import java.io.IOException;

import org.spearce.jgit.errors.IncorrectObjectTypeException;
import org.spearce.jgit.errors.MissingObjectException;
import org.spearce.jgit.revwalk.filter.RevFilter;

/**
 * An ordered list of {@link RevCommit} subclasses.
 * 
 * @param <E>
 *            type of subclass of RevCommit the list is storing.
 */
public class RevCommitList<E extends RevCommit> extends RevObjectList<E> {
	private RevWalk walker;

	@Override
	public void clear() {
		super.clear();
		walker = null;
	}

	/**
	 * Apply a flag to all commits matching the specified filter.
	 * <p>
	 * Same as <code>applyFlag(matching, flag, 0, size())</code>, but without
	 * the incremental behavior.
	 * 
	 * @param matching
	 *            the filter to test commits with. If the filter includes a
	 *            commit it will have the flag set; if the filter does not
	 *            include the commit the flag will be unset.
	 * @param flag
	 *            the flag to apply (or remove). Applications are responsible
	 *            for allocating this flag from the source RevWalk.
	 * @throws IOException
	 *             revision filter needed to read additional objects, but an
	 *             error occurred while reading the pack files or loose objects
	 *             of the repository.
	 * @throws IncorrectObjectTypeException
	 *             revision filter needed to read additional objects, but an
	 *             object was not of the correct type. Repository corruption may
	 *             have occurred.
	 * @throws MissingObjectException
	 *             revision filter needed to read additional objects, but an
	 *             object that should be present was not found. Repository
	 *             corruption may have occurred.
	 */
	public void applyFlag(final RevFilter matching, final RevFlag flag)
			throws MissingObjectException, IncorrectObjectTypeException,
			IOException {
		applyFlag(matching, flag, 0, size());
	}

	/**
	 * Apply a flag to all commits matching the specified filter.
	 * <p>
	 * This version allows incremental testing and application, such as from a
	 * background thread that needs to periodically halt processing and send
	 * updates to the UI.
	 * 
	 * @param matching
	 *            the filter to test commits with. If the filter includes a
	 *            commit it will have the flag set; if the filter does not
	 *            include the commit the flag will be unset.
	 * @param flag
	 *            the flag to apply (or remove). Applications are responsible
	 *            for allocating this flag from the source RevWalk.
	 * @param rangeBegin
	 *            first commit within the list to begin testing at, inclusive.
	 *            Must not be negative, but may be beyond the end of the list.
	 * @param rangeEnd
	 *            last commit within the list to end testing at, exclusive. If
	 *            smaller than or equal to <code>rangeBegin</code> then no
	 *            commits will be tested.
	 * @throws IOException
	 *             revision filter needed to read additional objects, but an
	 *             error occurred while reading the pack files or loose objects
	 *             of the repository.
	 * @throws IncorrectObjectTypeException
	 *             revision filter needed to read additional objects, but an
	 *             object was not of the correct type. Repository corruption may
	 *             have occurred.
	 * @throws MissingObjectException
	 *             revision filter needed to read additional objects, but an
	 *             object that should be present was not found. Repository
	 *             corruption may have occurred.
	 */
	public void applyFlag(final RevFilter matching, final RevFlag flag,
			int rangeBegin, int rangeEnd) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		final RevWalk w = flag.getRevWalk();
		rangeEnd = Math.min(rangeEnd, size());
		while (rangeBegin < rangeEnd) {
			int index = rangeBegin;
			Block s = contents;
			while (s.shift > 0) {
				final int i = index >> s.shift;
				index -= i << s.shift;
				s = (Block) s.contents[i];
			}

			while (rangeBegin++ < rangeEnd && index < BLOCK_SIZE) {
				final RevCommit c = (RevCommit) s.contents[index++];
				if (matching.include(w, c))
					c.add(flag);
				else
					c.remove(flag);
			}
		}
	}

	/**
	 * Remove the given flag from all commits.
	 * <p>
	 * Same as <code>clearFlag(flag, 0, size())</code>, but without the
	 * incremental behavior.
	 * 
	 * @param flag
	 *            the flag to remove. Applications are responsible for
	 *            allocating this flag from the source RevWalk.
	 */
	public void clearFlag(final RevFlag flag) {
		clearFlag(flag, 0, size());
	}

	/**
	 * Remove the given flag from all commits.
	 * <p>
	 * This method is actually implemented in terms of:
	 * <code>applyFlag(RevFilter.NONE, flag, rangeBegin, rangeEnd)</code>.
	 * 
	 * @param flag
	 *            the flag to remove. Applications are responsible for
	 *            allocating this flag from the source RevWalk.
	 * @param rangeBegin
	 *            first commit within the list to begin testing at, inclusive.
	 *            Must not be negative, but may be beyond the end of the list.
	 * @param rangeEnd
	 *            last commit within the list to end testing at, exclusive. If
	 *            smaller than or equal to <code>rangeBegin</code> then no
	 *            commits will be tested.
	 */
	public void clearFlag(final RevFlag flag, final int rangeBegin,
			final int rangeEnd) {
		try {
			applyFlag(RevFilter.NONE, flag, rangeBegin, rangeEnd);
		} catch (IOException e) {
			// Never happen. The filter we use does not throw any
			// exceptions, for any reason.
		}
	}

	/**
	 * Find the next commit that has the given flag set.
	 * 
	 * @param flag
	 *            the flag to test commits against.
	 * @param begin
	 *            first commit index to test at. Applications may wish to begin
	 *            at 0, to test the first commit in the list.
	 * @return index of the first commit at or after index <code>begin</code>
	 *         that has the specified flag set on it; -1 if no match is found.
	 */
	public int indexOf(final RevFlag flag, int begin) {
		while (begin < size()) {
			int index = begin;
			Block s = contents;
			while (s.shift > 0) {
				final int i = index >> s.shift;
				index -= i << s.shift;
				s = (Block) s.contents[i];
			}

			while (begin++ < size() && index < BLOCK_SIZE) {
				final RevCommit c = (RevCommit) s.contents[index++];
				if (c.has(flag))
					return begin;
			}
		}
		return -1;
	}

	/**
	 * Find the next commit that has the given flag set.
	 * 
	 * @param flag
	 *            the flag to test commits against.
	 * @param begin
	 *            first commit index to test at. Applications may wish to begin
	 *            at <code>size()-1</code>, to test the last commit in the
	 *            list.
	 * @return index of the first commit at or before index <code>begin</code>
	 *         that has the specified flag set on it; -1 if no match is found.
	 */
	public int lastIndexOf(final RevFlag flag, int begin) {
		begin = Math.min(begin, size() - 1);
		while (begin >= 0) {
			int index = begin;
			Block s = contents;
			while (s.shift > 0) {
				final int i = index >> s.shift;
				index -= i << s.shift;
				s = (Block) s.contents[i];
			}

			while (begin-- >= 0 && index >= 0) {
				final RevCommit c = (RevCommit) s.contents[index--];
				if (c.has(flag))
					return begin;
			}
		}
		return -1;
	}

	/**
	 * Set the revision walker this list populates itself from.
	 * 
	 * @param w
	 *            the walker to populate from.
	 * @see #fillTo(int)
	 */
	public void source(final RevWalk w) {
		walker = w;
	}

	/**
	 * Is this list still pending more items?
	 * 
	 * @return true if {@link #fillTo(int)} might be able to extend the list
	 *         size when called.
	 */
	public boolean isPending() {
		return walker != null;
	}

	/**
	 * Ensure this list contains at least a specified number of commits.
	 * <p>
	 * The revision walker specified by {@link #source(RevWalk)} is pumped until
	 * the given number of commits are contained in this list. If there are
	 * fewer total commits available from the walk then the method will return
	 * early. Callers can test the final size of the list by {@link #size()} to
	 * determine if the high water mark specified was met.
	 * 
	 * @param highMark
	 *            number of commits the caller wants this list to contain when
	 *            the fill operation is complete.
	 * @throws IOException
	 *             see {@link RevWalk#next()}
	 * @throws IncorrectObjectTypeException
	 *             see {@link RevWalk#next()}
	 * @throws MissingObjectException
	 *             see {@link RevWalk#next()}
	 */
	public void fillTo(final int highMark) throws MissingObjectException,
			IncorrectObjectTypeException, IOException {
		if (walker == null || size > highMark)
			return;

		Generator p = walker.pending;
		RevCommit c = p.next();
		if (c == null) {
			walker.pending = EndGenerator.INSTANCE;
			walker = null;
			return;
		}
		enter(size, (E) c);
		add((E) c);
		p = walker.pending;

		while (size <= highMark) {
			int index = size;
			Block s = contents;
			while (index >> s.shift >= BLOCK_SIZE) {
				s = new Block(s.shift + BLOCK_SHIFT);
				s.contents[0] = contents;
				contents = s;
			}
			while (s.shift > 0) {
				final int i = index >> s.shift;
				index -= i << s.shift;
				if (s.contents[i] == null)
					s.contents[i] = new Block(s.shift - BLOCK_SHIFT);
				s = (Block) s.contents[i];
			}

			final Object[] dst = s.contents;
			while (size <= highMark && index < BLOCK_SIZE) {
				c = p.next();
				if (c == null) {
					walker.pending = EndGenerator.INSTANCE;
					walker = null;
					return;
				}
				enter(size++, (E) c);
				dst[index++] = c;
			}
		}
	}

	/**
	 * Optional callback invoked when commits enter the list by fillTo.
	 * <p>
	 * This method is only called during {@link #fillTo(int)}.
	 * 
	 * @param index
	 *            the list position this object will appear at.
	 * @param e
	 *            the object being added (or set) into the list.
	 */
	protected void enter(final int index, final E e) {
		// Do nothing by default.
	}
}
