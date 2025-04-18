package org.clockworx.vampire.accumulator;

import java.util.Objects;

/**
 * Represents a value accumulator with minimum and maximum bounds.
 * This class provides thread-safe operations for managing a value within specified limits.
 */
public abstract class Accumulator
{
	private double value;
	private final double min;
	private final double max;

	protected Accumulator(double value, double min, double max)
	{
		this.min = min;
		this.max = max;
		setValue(value); // This will enforce the bounds
	}

	/**
	 * Gets the current value of the accumulator.
	 * @return The current value
	 */
	public synchronized double getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the accumulator, enforcing the min/max bounds.
	 * @param value The new value to set
	 */
	public synchronized void setValue(double value)
	{
		this.value = Math.min(Math.max(value, min), max);
	}

	/**
	 * Adds a value to the accumulator, enforcing the min/max bounds.
	 * @param value The value to add
	 */
	public synchronized void addValue(double value)
	{
		setValue(getValue() + value);
	}

	/**
	 * Gets the minimum allowed value.
	 * @return The minimum value
	 */
	public double getMin()
	{
		return min;
	}

	/**
	 * Gets the maximum allowed value.
	 * @return The maximum value
	 */
	public double getMax()
	{
		return max;
	}

	/**
	 * Checks if the accumulator is at its minimum value.
	 * @return true if at minimum, false otherwise
	 */
	public synchronized boolean isAtMin()
	{
		return value <= min;
	}

	/**
	 * Checks if the accumulator is at its maximum value.
	 * @return true if at maximum, false otherwise
	 */
	public synchronized boolean isAtMax()
	{
		return value >= max;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Accumulator that = (Accumulator) o;
		return Double.compare(that.value, value) == 0 &&
			   Double.compare(that.min, min) == 0 &&
			   Double.compare(that.max, max) == 0;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(value, min, max);
	}
}
