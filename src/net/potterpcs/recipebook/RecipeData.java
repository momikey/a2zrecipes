package net.potterpcs.recipebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecipeData {

	private final String TAG = RecipeData.class.getSimpleName();
	
	static final int DB_VERSION = 5;
	static final String DB_FILENAME = "recipebook.db";
	
	static final String RECIPES_TABLE = "recipes";
	public static final String RT_ID = "_id";
	public static final String RT_NAME = "name";
	public static final String RT_DESCRIPTION = "description";
	public static final String RT_RATING = "rating";
	public static final String RT_CREATOR = "creator";
	public static final String RT_DATE = "date";
	public static final String RT_SERVING = "serving";
	public static final String RT_TIME = "time";
	public static final String RT_PHOTO = "photo";
	public static final String[] RECIPES_FIELDS = { RT_ID, RT_NAME,
			RT_DESCRIPTION, RT_RATING, RT_CREATOR, RT_DATE, RT_SERVING, RT_TIME, RT_PHOTO };
	
	static final String INGREDIENTS_TABLE = "ingredients";
	public static final String IT_ID = "_id";
	public static final String IT_NAME = "name";
	public static final String IT_RECIPE_ID = "recipe_id";
	public static final String[] INGREDIENTS_FIELDS = { IT_ID, IT_NAME };
	
	static final String DIRECTIONS_TABLE = "directions";
	public static final String DT_ID = "_id";
	public static final String DT_STEP = "step";
	public static final String DT_SEQUENCE = "sequence";
	public static final String DT_RECIPE_ID = "recipe_id";
	public static final String[] DIRECTIONS_FIELDS = { DT_ID, DT_STEP, DT_SEQUENCE };
	
	static final String TAGS_TABLE = "tags";
	public static final String TT_ID = "_id";
	public static final String TT_TAG = "tag";
	public static final String TT_RECIPE_ID = "recipe_id";
	public static final String[] TAGS_FIELDS = { TT_ID, TT_TAG };
	
	public static class Recipe {
		long id;
		String name;
		String description;
		float rating;
		String creator;
		String date;
		int serving;
		int time;
		String[] ingredients;
		String[] directions;
		String[] tags;
		String photo;
	}
	
	class DbHelper extends SQLiteOpenHelper {
		private static final String RT_FOREIGN_KEY = " integer references " + RECIPES_TABLE + "("
							+ RT_ID + ") on delete restrict deferrable initially deferred)";

		public DbHelper(Context context) {
			super(context, DB_FILENAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DB_FILENAME);
			
			// create main table
			db.execSQL("create table " + RECIPES_TABLE + " (" + RT_ID + " integer primary key, "
					+ RT_NAME + " text, " + RT_DESCRIPTION + " text, " + RT_RATING + " real, "
					+ RT_CREATOR + " text, " + RT_DATE + " text, " + RT_SERVING + " integer, " 
					+ RT_PHOTO + " text, " + RT_TIME + " integer)");
			
			// create ingredients table
			db.execSQL("create table " + INGREDIENTS_TABLE + " (" + IT_ID + " integer primary key, " 
					+ IT_NAME + " text, " + IT_RECIPE_ID + RT_FOREIGN_KEY);
			
			// create directions table
			db.execSQL("create table " + DIRECTIONS_TABLE + " (" + DT_ID + " integer primary key, "
					+ DT_STEP + " text, " + DT_SEQUENCE + " int, " + DT_RECIPE_ID + RT_FOREIGN_KEY);
			
			// create tags table
			db.execSQL("create table " + TAGS_TABLE + " (" + TT_ID + " integer primary key, "
					+ TT_TAG + " text, " + TT_RECIPE_ID + RT_FOREIGN_KEY);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Change this to properly update db
			if (newVersion == 5) {
				db.execSQL("alter table " + RECIPES_TABLE + " add column " + RT_PHOTO + " text");
			} else if (oldVersion < 4) {
				db.execSQL("drop table " + RECIPES_TABLE);
				db.execSQL("drop table " + INGREDIENTS_TABLE);
				db.execSQL("drop table " + DIRECTIONS_TABLE);
				db.execSQL("drop table " + TAGS_TABLE);
				this.onCreate(db);			
			}
		}
	}
	
	private final DbHelper dbHelper;
	
	public RecipeData(Context context) {
		dbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized database");
	}
	
	public void close() {
		dbHelper.close();
	}
	
	private String createInnerJoin(String left, String right, String left_id, String right_id) {
		return left + " inner join " + right + " on " + left_id + " = " + right_id;
	}
	
//	private String createLikePattern(String[] strings) {
	private String createLikePattern() {
		// FIXME hack until Google fixes query bug #3153
//		return RT_NAME + " like '%" + strings[0] + "%' or " + RT_DESCRIPTION + " like '%" + strings[1] + "%'";
		return RT_NAME + " like ? or " + RT_DESCRIPTION + " like ?";
	}
	
	public long getLastInsertRecipeId() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(RECIPES_TABLE, new String[] { "last_insert_rowid() " }, null, null, null, null, null);
		c.moveToFirst();
		long id = c.getLong(0);
		c.close();
		return id;
	}
	
	public long getNumberOfRecipes() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(RECIPES_TABLE, new String[] { "_id" }, null, null, null, null, null);
		long num = c.getCount();
		c.close();
		return num;
	}
	
	public Cursor getAllRecipes(String sortBy) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(RECIPES_TABLE, RECIPES_FIELDS, null, null, null,
				null, sortBy);
	}
	
	public Cursor getAllRecipes() {
		return getAllRecipes(RT_NAME);
	}
	
	public Cursor getAllRecipes(String[] columns, String sortBy) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(RECIPES_TABLE, columns, null, null, null, null, sortBy);
	}
	
	public Cursor getAllRecipes(String[] columns) {
		return getAllRecipes(columns, RT_NAME);
	}
	
	public Cursor getMatchingRecipes(String match, String sortBy) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
//		return db.query(RECIPES_TABLE, RECIPES_FIELDS, createLikePattern(new String[] { match, match }), null, null,
//				null, RT_NAME);
		// FIXME Android bug 3153
		String fixed = "%" + match + "%";
		return db.query(RECIPES_TABLE, RECIPES_FIELDS, createLikePattern(), new String[] { fixed, fixed }, null,
				null, sortBy);
	}
	
	public Cursor getMatchingRecipes(String match) {
		return getMatchingRecipes(match, RT_NAME);
	}
	
	public Cursor getSingleRecipe(long rid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(RECIPES_TABLE, RECIPES_FIELDS, RT_ID + " = ?", 
				new String[] { Long.toString(rid) }, null, null, null);
	}
	
	public Recipe getSingleRecipeObject(long rid) {
		Recipe r = new Recipe();
		Cursor c = getSingleRecipe(rid);
		c.moveToFirst();
		r.id = c.getLong(c.getColumnIndex(RT_ID));
		r.name = c.getString(c.getColumnIndex(RT_NAME));
		r.description = c.getString(c.getColumnIndex(RT_DESCRIPTION));
		r.rating = c.getFloat(c.getColumnIndex(RT_RATING));
		r.creator = c.getString(c.getColumnIndex(RT_CREATOR));
		r.date = c.getString(c.getColumnIndex(RT_DATE));
		r.serving = c.getInt(c.getColumnIndex(RT_SERVING));
		r.time = c.getInt(c.getColumnIndex(RT_TIME));
		r.photo = c.getString(c.getColumnIndex(RT_PHOTO));
		
		r.ingredients = getRecipeIngredientStrings(rid);
		r.directions = getRecipeDirectionStrings(rid);
		r.tags = getRecipeTagStrings(rid);
		
		c.close();
		return r;
	}
	
	public Cursor getRecipeIngredients(long rid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(INGREDIENTS_TABLE, INGREDIENTS_FIELDS, IT_RECIPE_ID + " = ?", 
				new String[] { Long.toString(rid) }, null, null, null);
	}
	
	public String[] getRecipeIngredientStrings(long rid) {
		Cursor c = getRecipeIngredients(rid);
		String[] ings = new String[c.getCount()];
		
		c.moveToFirst();
		while (!c.isAfterLast()) {
			ings[c.getPosition()] = c.getString(c.getColumnIndex(IT_NAME));
			c.moveToNext();
		}
		
		c.close();
		return ings;
	}
	
	public Cursor getRecipeDirections(long rid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(DIRECTIONS_TABLE, DIRECTIONS_FIELDS, DT_RECIPE_ID + " = ?", 
				new String[] { Long.toString(rid) }, null, null, DT_SEQUENCE);
	}

	public String[] getRecipeDirectionStrings(long rid) {
		// NOTE: the returned array is in database sequential order
		Cursor c = getRecipeDirections(rid);
		String[] dirs = new String[c.getCount()];
		
		c.moveToFirst();
		while (!c.isAfterLast()) {
			dirs[c.getPosition()] = c.getString(c.getColumnIndex(DT_STEP));
			c.moveToNext();
		}
		
		c.close();
		return dirs;
	}
	
	public Cursor getRecipeTags(long rid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TAGS_TABLE, TAGS_FIELDS, TT_RECIPE_ID + " = ?", 
				new String[] { Long.toString(rid) }, null, null, null);
	}

	public String[] getRecipeTagStrings(long rid) {
		Cursor c = getRecipeTags(rid);
		String[] tags = new String[c.getCount()];
		
		c.moveToFirst();
		while (!c.isAfterLast()) {
			tags[c.getPosition()] = c.getString(c.getColumnIndex(TT_TAG));
			c.moveToNext();
		}
		
		c.close();
		return tags;
	}
	
	public Cursor getRecipesByTag(String tag, String sortBy) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(createInnerJoin(RECIPES_TABLE, TAGS_TABLE, RT_ID, TT_ID), RECIPES_FIELDS,
				TT_TAG + " = ?", new String[] { tag }, null, null, sortBy);
	}
	
	public Cursor getRecipesByTag(String tag) {
		return getRecipesByTag(tag, RT_NAME);
	}
	
	public void insertRecipe(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(RECIPES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void insertIngredients(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(INGREDIENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void insertDirections(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(DIRECTIONS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void insertTags(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TAGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void insertRecipe(Recipe r) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			ContentValues values = createRecipeForInsert(r);
			long rowid = db.insertWithOnConflict(RECIPES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			
			for (String ing : r.ingredients) {
				ContentValues cvi = createIngredientsCV(rowid, ing);
				db.insertWithOnConflict(INGREDIENTS_TABLE, null, cvi, SQLiteDatabase.CONFLICT_IGNORE);
			}
			
			int step = 1;
			for (String dir : r.directions) {
				ContentValues cdirs = createDirectionsCV(rowid, step, dir);
				db.insertWithOnConflict(DIRECTIONS_TABLE, null, cdirs, SQLiteDatabase.CONFLICT_IGNORE);
				step++;
			}
			
			for (String tag : r.tags) {
				ContentValues ctags = createTagsCV(rowid, tag);
				db.insertWithOnConflict(TAGS_TABLE, null, ctags, SQLiteDatabase.CONFLICT_IGNORE);
			}
			
		} finally {
			db.close();
		}
	}

	public ContentValues createTagsCV(long rowid, String tag) {
		ContentValues ctags = new ContentValues();
		ctags.put(TT_TAG, tag);
		ctags.put(TT_RECIPE_ID, rowid);
		return ctags;
	}

	public ContentValues createDirectionsCV(long rowid, int step, String dir) {
		ContentValues cdirs = new ContentValues();
		cdirs.put(DT_STEP, dir);
		cdirs.put(DT_SEQUENCE, step);
		cdirs.put(DT_RECIPE_ID, rowid);
		return cdirs;
	}

	public ContentValues createIngredientsCV(long rowid, String ing) {
		ContentValues cvi = new ContentValues();
		cvi.put(IT_NAME, ing);
		cvi.put(IT_RECIPE_ID, rowid);
		return cvi;
	}

	public ContentValues createRecipeForInsert(Recipe r) {
		ContentValues values = new ContentValues();
		values.put(RT_NAME, r.name);
		values.put(RT_DESCRIPTION, r.description);
		values.put(RT_RATING, r.rating);
		values.put(RT_CREATOR, r.creator);
		values.put(RT_DATE, r.date);
		values.put(RT_SERVING, r.serving);
		values.put(RT_TIME, r.time);
		return values;
	}
	
	public void deleteRecipe(long rid) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.delete(INGREDIENTS_TABLE, IT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
			db.delete(DIRECTIONS_TABLE, DT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
			db.delete(TAGS_TABLE, TT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
			db.delete(RECIPES_TABLE, RT_ID + " = ?", new String[] { Long.toString(rid) });
		} finally {
			db.close();
		}
	}
	
	public void updateRecipe(Recipe r) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			long rid = r.id;
			String[] whereArgs = { Long.toString(rid) };
			db.updateWithOnConflict(RECIPES_TABLE, createRecipeForInsert(r), RT_ID + " = ?", 
					new String[] { Long.toString(r.id) }, SQLiteDatabase.CONFLICT_IGNORE);
			
			// until we can figure out a smarter way to update
			db.delete(INGREDIENTS_TABLE, IT_RECIPE_ID + " = ?", whereArgs);
			for (String ing : r.ingredients) {
//				insertIngredients(createIngredientsCV(rid, ing));
				db.insertWithOnConflict(INGREDIENTS_TABLE, null, createIngredientsCV(rid, ing), 
						SQLiteDatabase.CONFLICT_IGNORE);
			}
			
			db.delete(DIRECTIONS_TABLE, DT_RECIPE_ID + " = ?", whereArgs);
			int step = 1;
			for (String dir : r.directions) {
//				insertDirections(createDirectionsCV(rid, step, dir));
				db.insertWithOnConflict(DIRECTIONS_TABLE, null, createDirectionsCV(rid, step, dir), 
						SQLiteDatabase.CONFLICT_IGNORE);
				step++;
			}
			
			db.delete(TAGS_TABLE, TT_RECIPE_ID + " = ?", whereArgs);
			for (String tag : r.tags) {
//				insertTags(createTagsCV(rid, tag));
				db.insertWithOnConflict(TAGS_TABLE, null, createTagsCV(rid, tag), 
						SQLiteDatabase.CONFLICT_IGNORE);
			}
			
		} finally {
			db.close();
		}
	}
	
	public void updateRecipe(long rid, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.updateWithOnConflict(RECIPES_TABLE, values, RT_ID + " = ?", 
					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void updateIngredients(long rid, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.updateWithOnConflict(INGREDIENTS_TABLE, values, IT_RECIPE_ID + " = ?", 
					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}		
	}
	
	public void updateDirections(long rid, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.updateWithOnConflict(DIRECTIONS_TABLE, values, DT_RECIPE_ID + " = ?", 
					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
	
	public void updateTags(long rid, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.updateWithOnConflict(TAGS_TABLE, values, TT_RECIPE_ID + " = ?", 
					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			db.close();
		}
	}
}
