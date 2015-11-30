package dataCoverage;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import output.OutputDataFile;

import knowledgeBase.BindingsOperations;

import data.Constant;
import data.Term;
import data.Variable;

public class Coverage implements Cloneable {

	private Set<Bindings> positiveBindings = null;
	private Set<Bindings> negativeBindings = null;

	public Coverage() {
		positiveBindings = new HashSet<Bindings>();
		negativeBindings = new HashSet<Bindings>();
	}

	public Set<Bindings> getPositiveBindings() {
		return Collections.unmodifiableSet(this.positiveBindings);
	}

	public Set<Bindings> getNegativeBindings() {
		return Collections.unmodifiableSet(this.negativeBindings);
	}

	public void addPositiveBinding(Bindings b) {
		positiveBindings.add(b);
	}

	public void addNegativeBinding(Bindings b) {
		negativeBindings.add(b);
	}

	public void addPositiveBindingsSet(Set<Bindings> positiveBindings) {
		this.positiveBindings.addAll(positiveBindings);
	}

	public void addNegativeBindingsSet(Set<Bindings> negativeBindings) {
		this.negativeBindings.addAll(negativeBindings);
	}

	public void setPositiveBindings(Set<Bindings> positiveBindings) {
		this.positiveBindings = positiveBindings;
	}

	public void setNegativeBindings(Set<Bindings> negativeBindings) {
		this.negativeBindings = negativeBindings;
	}

	/**
	 * Gets the coverage as a string
	 * @param trace 
	 * 
	 * @return a string with all data of the this coverage
	 * @throws IOException 
	 */
	public void writeToFile(OutputDataFile trace) throws IOException {
		if (!this.positiveBindings.isEmpty()) {
			trace.writeToFile("Positive Bindings\n");
			for (Bindings b : this.positiveBindings)
				trace.writeToFile(b.toString() + "\n");
		}
		if (!this.negativeBindings.isEmpty()) {
			trace.writeToFile("Negative Bindings\n");
			for (Bindings b : this.negativeBindings)
				trace.writeToFile(b.toString() + "\n");
		}
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (!this.positiveBindings.isEmpty()) {
			result.append("Positive Bindings\n");
			for (Bindings b : this.positiveBindings)
				result.append(b.toString() + "\n");
		}
		if (!this.negativeBindings.isEmpty()) {
			result.append("Negative Bindings\n");
			for (Bindings b : this.negativeBindings)
				result.append(b.toString() + "\n");
		}
		return result.toString();
	}
	
	/**
	 * Checks if two coverage objets are equals
	 * 
	 * @param cr
	 *            coverage to compare
	 * @return true if both of coverage are equals, false otherwise
	 */
	public boolean equals(Coverage cr) {
		boolean result = false;
		boolean end = false;
		Set<Bindings> posBindings = cr.getPositiveBindings();
		Set<Bindings> negBindings = cr.getNegativeBindings();

		if (this.positiveBindings.size() == posBindings.size()
				&& this.negativeBindings.size() == negBindings.size()) {
			Iterator<Bindings> it = this.positiveBindings.iterator();
			while (it.hasNext() && !end) {
				if (!BindingsOperations.belongs(it.next(), posBindings))
					end = true;
			}

			it = this.negativeBindings.iterator();
			while (it.hasNext() && !end) {
				if (!BindingsOperations.belongs(it.next(), negBindings))
					end = true;
			}

			if (!end)
				result = true;
		} else
			result = false;

		return result;
	}

	/**
	 * Remove a term from the set of bindings both the positive ones and the
	 * negative ones.
	 * 
	 * @param t
	 *            term to remove of the coverage
	 */
	public void removeVar(Term t) {

		Set<Bindings> auxPosBindings = new HashSet<Bindings>();
		Set<Bindings> auxNegBindings = new HashSet<Bindings>();

		Bindings auxBinding = null;
		Bindings b = null;
		boolean ok = true;
		Map<Variable, Constant> binding = null;

		Iterator<Bindings> it = this.positiveBindings.iterator();

		if (it.hasNext()) {
			if (it.next().getBindings().containsKey(t)) {

				it = this.positiveBindings.iterator();
				while (it.hasNext()) {
					b = it.next();
					binding = b.getBindings();
					auxBinding = new Bindings();
					for (Iterator<Variable> iterator = binding.keySet()
							.iterator(); iterator.hasNext();) {
						Variable v = iterator.next();
						if (!t.equals(v))
							auxBinding.addBinding(v, binding.get(v));
					}
					auxPosBindings.add(auxBinding);
				}
				this.positiveBindings = auxPosBindings;

				it = this.negativeBindings.iterator();
				while (it.hasNext()) {
					b = it.next();
					binding = b.getBindings();
					auxBinding = new Bindings();
					ok = true;
					for (Iterator<Variable> iterator = binding.keySet()
							.iterator(); iterator.hasNext() && ok;) {
						Variable v = iterator.next();
						if (!t.equals(v))
							auxBinding.addBinding(v, binding.get(v));
					}
					auxNegBindings.add(auxBinding);
				}
				this.negativeBindings = auxNegBindings;
			}
		}
	}

	@Override
	public Coverage clone() {
		Coverage clone = new Coverage();

		// deep clone

		clone.setPositiveBindings(new HashSet<Bindings>(this.positiveBindings));
		clone.setNegativeBindings(new HashSet<Bindings>(this.negativeBindings));

		return clone;
	}

	public Bindings getFirstPositiveBinding() {
		Bindings result = new Bindings();
		Iterator<Bindings> it = this.positiveBindings.iterator();
		if (it.hasNext())
			result = it.next();
		return result;
	}
}