package com.milanix.example.downloader.data.database.util;

/**
 * This class contains helper util for creating query
 * 
 * @author Milan
 * 
 */
public class QueryHelper {

	public static final String ORDERING_DESC = "DESC";
	public static final String ORDERING_ASC = "ASC";

	/**
	 * This method will get ordering string with given param. It doesn't
	 * validate passed parameter
	 * 
	 * @param fieldName
	 *            is the field to sort by
	 * @param ordering
	 *            is a sort type
	 * @return ordering string
	 */
	public static String getOrdering(String fieldName, String ordering) {
		final StringBuilder orderingBuilder = new StringBuilder("");
		orderingBuilder.append(fieldName);
		orderingBuilder.append(" ");
		orderingBuilder.append(ordering);

		return orderingBuilder.toString();
	}

	/**
	 * This method will get query where clause string with given param
	 * 
	 * @param fieldName
	 *            is the field to add where
	 * @param value
	 *            is a value to filter by
	 * @param isEqual
	 *            to modify equals or not
	 * @return where clause
	 */
	public static String getWhere(String fieldName, String value,
			boolean isEqual) {
		final StringBuilder whereClauseBuilder = new StringBuilder("");
		whereClauseBuilder.append(fieldName);

		if (isEqual)
			whereClauseBuilder.append("='");
		else
			whereClauseBuilder.append("!='");

		whereClauseBuilder.append(value);
		whereClauseBuilder.append("'");

		return whereClauseBuilder.toString();
	}

	/**
	 * This method will get query where clause string with given param
	 * 
	 * @param fieldName
	 *            is the field to add where
	 * @param value
	 *            is a value to filter by
	 * @param isEqual
	 *            to modify equals or not
	 * @return where clause
	 */
	public static String getWhere(String fieldName, Number value,
			boolean isEqual) {
		final StringBuilder whereClauseBuilder = new StringBuilder("");
		whereClauseBuilder.append(fieldName);

		if (isEqual)
			whereClauseBuilder.append("=");
		else
			whereClauseBuilder.append("!=");

		whereClauseBuilder.append(value);

		return whereClauseBuilder.toString();
	}

	/**
	 * This method will get query in operator string with given param
	 * 
	 * @param fieldName
	 *            is the field to add where
	 * @param ids
	 *            long
	 * @return in
	 */
	public static String getLongIn(String fieldName, long[] ids) {
		final StringBuilder inClauseBuilder = new StringBuilder("");
		inClauseBuilder.append(fieldName);
		inClauseBuilder.append(" in (");

		for (int i = 0; i < ids.length; i++) {
			inClauseBuilder.append(Long.toString(ids[i]));

			if (i < ids.length - 1)
				inClauseBuilder.append(",");
		}

		inClauseBuilder.append(")");

		return inClauseBuilder.toString();
	}

}
