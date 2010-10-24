package tasks;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import system.ResultImpl;
import api.Result;
import api.Task;

/**
 * Computes an optimal solution for the <a
 * href="http://en.wikipedia.org/wiki/Travelling_salesman_problem">Travelling
 * Salesman Problem</a>
 * 
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 */
public class TspTask extends TaskBase<List<TspTask.City>> implements
		Serializable {

	/**
	 * Represents a city in the travelling salesman problem by defining its
	 * x-coordinate, y-coordinate and label.
	 * 
	 * @author Manasa Chandrasekhar
	 * @author Kowshik Prakasam*
	 */
	public class City implements Serializable {

		private static final long serialVersionUID = -8660442769258565881L;
		private double x;
		private double y;
		private int label;

		public City(int label, double x, double y) {
			this.x = x;
			this.y = y;
			this.label = label;
		}

		public void setX(int x) {
			this.x = x;
		}

		public double getX() {
			return x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public double getY() {
			return y;
		}

		public void setLabel(int label) {
			this.label = label;
		}

		public int getLabel() {
			return label;
		}

		public String toString() {
			return "" + label;
		}

		@Override
		public boolean equals(Object c) {
			return this.getLabel() == (((City) c).getLabel());
		}

	}

	private static final long serialVersionUID = 3276207466199157936L;
	private List<City> citiesList;
	private City startCity;
	private static final int NUMBER_OF_LEVELS = 2;
	private int numberOfChildren;

	/**
	 * @param cities
	 *            Represents the x and y coordinates of cities. cities[i][0] is
	 *            the x-coordinate of city[i] and cities[i][1] is the
	 *            y-coordinate of city[i].
	 */

	public TspTask(double[][] cities) {
		super(DEFAULT_TASK_ID, DEFAULT_TASK_ID, Task.Status.DECOMPOSE, System
				.currentTimeMillis());
		citiesList = new Vector<City>();
		for (int cityIndex = 0; cityIndex < cities.length; cityIndex++) {
			citiesList.add(new City(cityIndex, cities[cityIndex][0],
					cities[cityIndex][1]));
		}
		this.startCity = new City(0, cities[0][0], cities[0][1]);
		this.numberOfChildren = citiesList.size() - 1;
	}

	private TspTask(City startCity, List<City> citiesList, String taskId,
			String parentId, Task.Status s) {
		super(taskId, parentId, Task.Status.DECOMPOSE, System
				.currentTimeMillis());
		this.citiesList = citiesList;
		this.startCity = startCity;
		this.numberOfChildren = citiesList.size();
	}

	@Override
	/**
	 * Implements the decompose phase of TSP divide and conquer solution
	 */
	public Result<List<City>> decompose() {

		/*
		 * Before decomposition, compute a new lower bound using the 2 shortest
		 * edges incident on each city and compare the new value with the latest
		 * upper bound in shared object.
		 * 
		 * If the new lower bound is greater than the best possible upper bound
		 * at that instant, then the node need not be explored further. Create a
		 * ResultImpl object with no values and no subtasks and return it.
		 * 
		 * Else, continue with TSP decomposition.
		 */

		int level = this.getTaskLevel();
		if (level < NUMBER_OF_LEVELS) {

			List<Task<List<City>>> subTasks = new Vector<Task<List<City>>>();
			List<String> childIds = this.getChildIds();
			int childIndex = 0;
			for (int i = 0; i < citiesList.size(); i++) {
				if (!citiesList.get(i).equals(this.startCity)) {
					City newStartCity = citiesList.get(i);
					String childId = childIds.get(childIndex);
					childIndex++;
					List<City> childCities = new Vector<City>();
					for (int j = 0; j < citiesList.size(); j++) {
						if (!citiesList.get(j).equals(this.startCity)
								&& !citiesList.get(j).equals(newStartCity)) {
							childCities.add(citiesList.get(j));
						}
					}
					TspTask childTask = new TspTask(newStartCity, childCities,
							childId, this.getId(), Task.Status.DECOMPOSE);
					subTasks.add(childTask);
				}
			}
			return new ResultImpl<List<City>>(this.getStartTime(),
					System.currentTimeMillis(), subTasks);
		}
		List<City> minRoute = findMinRoute();

		return new ResultImpl<List<City>>(this.getStartTime(),
				System.currentTimeMillis(), minRoute);

	}

	// Finds minimum route for the current list of cities
	private List<City> findMinRoute() {
		int[] permutation = new int[citiesList.size()];
		for (int i = 0; i < citiesList.size(); i++) {
			permutation[i] = i;
		}
		Arrays.sort(permutation);
		double minLength = Double.MAX_VALUE;
		int[] minRoute = permutation.clone();
		do {
			double thisLength = findLength(this.startCity,
					citiesList.get(permutation[0]));
			int i;

			for (i = 0; i < permutation.length - 1; i++) {
				thisLength += findLength(citiesList.get(permutation[i]),
						citiesList.get(permutation[i + 1]));
			}

			if (thisLength < minLength) {
				minLength = thisLength;
				minRoute = permutation.clone();
			}

		} while (nextPermutation(permutation));

		List<City> minCitiesRoute = new Vector<City>();
		for (int i = 0; i < minRoute.length; i++) {
			minCitiesRoute.add(citiesList.get(minRoute[i]));
		}
		minCitiesRoute.add(0, this.startCity);
		return minCitiesRoute;
	}

	/**
	 * Works with <a
	 * href='http://en.wikipedia.org/wiki/Permutation'>permutations</a> Accepts
	 * an array of <b>ints</b> and reorders it's elements to recieve
	 * lexicographically next permutation
	 * 
	 * @param p
	 *            permutation
	 * @return false, if given array is lexicographically last permutation, true
	 *         otherwise
	 */

	private boolean nextPermutation(int[] p) {
		int a = p.length - 2;
		while (a >= 0 && p[a] >= p[a + 1]) {
			a--;
		}
		if (a == -1) {
			return false;
		}
		int b = p.length - 1;
		while (p[b] <= p[a]) {
			b--;
		}
		int t = p[a];
		p[a] = p[b];
		p[b] = t;
		for (int i = a + 1, j = p.length - 1; i < j; i++, j--) {
			t = p[i];
			p[i] = p[j];
			p[j] = t;
		}
		return true;
	}

	/**
	 * Computes the distance between two points
	 */
	private double findLength(City c1, City c2) {
		double x1 = c1.getX();
		double y1 = c1.getY();
		double x2 = c2.getX();
		double y2 = c2.getY();
		return Point2D.distance(x1, y1, x2, y2);

	}

	@Override
	/**
	 * Number of subtasks created in each stage of recursion
	 */
	public int getDecompositionSize() {
		return this.numberOfChildren;
	}

	@Override
	/**
	 * Implements the conquer phase of TSP divide and conquer solution
	 */
	public Result<List<City>> compose(List<?> list) {

		/*
		 * The parameter list may contain null values since a Node can die due
		 * to pruningSo before type casting to List<List<City>>, do a null
		 * check.
		 * 
		 * If the list is null, then create an empty ResultImpl object and
		 * return it.
		 * 
		 * Else, create a ResultImpl object and return the route with
		 * the minimum distance
		 */

		List<List<City>> minRoutes = (List<List<City>>) list;
		List<City> chosenMinRoute = null;
		double minLength = Double.MAX_VALUE;
		int level = this.getTaskLevel();
		for (List<City> route : minRoutes) {
			City routeStartCity = route.get(0);

			double thisLength = findRouteLength(route)
					+ findLength(this.startCity, routeStartCity);
			if (level == DEFAULT_TASK_LEVEL) {
				City routeEndCity = route.get(route.size() - 1);
				thisLength += findLength(this.startCity, routeEndCity);
			}

			if (thisLength < minLength) {
				minLength = thisLength;
				chosenMinRoute = route;
			}
		}

		chosenMinRoute.add(0, this.startCity);
		return new ResultImpl<List<City>>(this.getStartTime(),
				System.currentTimeMillis(), chosenMinRoute);
	}

	private double findRouteLength(List<City> aListOfCities) {
		double length = 0;
		int i;
		for (i = 0; i < aListOfCities.size() - 1; i++) {
			length += findLength(aListOfCities.get(i), aListOfCities.get(i + 1));
		}
		return length;
	}

}
