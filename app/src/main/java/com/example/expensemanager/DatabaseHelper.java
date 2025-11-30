package com.example.expensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "expenses.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "timestamp INTEGER," +
                "date TEXT," +
                "type TEXT," +
                "category TEXT," +
                "amount REAL," +
                "note TEXT" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no-op for v1
    }

    public long insert(Expense e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("timestamp", e.timestamp);
        cv.put("date", e.date);
        cv.put("type", e.type);
        cv.put("category", e.category);
        cv.put("amount", e.amount);
        cv.put("note", e.note);
        return db.insert(TABLE, null, cv);
    }

    public ArrayList<Expense> listAll() {
        ArrayList<Expense> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, null, null, null, null, "timestamp DESC");
        while (c.moveToNext()) {
            Expense e = new Expense();
            e.id = c.getLong(c.getColumnIndex("id"));
            e.timestamp = c.getLong(c.getColumnIndex("timestamp"));
            e.date = c.getString(c.getColumnIndex("date"));
            e.type = c.getString(c.getColumnIndex("type"));
            e.category = c.getString(c.getColumnIndex("category"));
            e.amount = c.getDouble(c.getColumnIndex("amount"));
            e.note = c.getString(c.getColumnIndex("note"));
            out.add(e);
        }
        c.close();
        return out;
    }

    public ArrayList<Expense> listForMonth(int year, int month) {
        // month is 1-12
        ArrayList<Expense> out = new ArrayList<>();
        String prefix = String.format(Locale.US, "%04d-%02d-", year, month);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, "date LIKE ?", new String[]{prefix + "%"}, null, null, "timestamp DESC");
        while (c.moveToNext()) {
            Expense e = new Expense();
            e.id = c.getLong(c.getColumnIndex("id"));
            e.timestamp = c.getLong(c.getColumnIndex("timestamp"));
            e.date = c.getString(c.getColumnIndex("date"));
            e.type = c.getString(c.getColumnIndex("type"));
            e.category = c.getString(c.getColumnIndex("category"));
            e.amount = c.getDouble(c.getColumnIndex("amount"));
            e.note = c.getString(c.getColumnIndex("note"));
            out.add(e);
        }
        c.close();
        return out;
    }

    public double totalForMonthAndType(int year, int month, String type) {
        String prefix = String.format(Locale.US, "%04d-%02d-", year, month);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(amount) FROM " + TABLE + " WHERE date LIKE ? AND type = ?", new String[]{prefix + "%", type});
        double val = 0.0;
        if (c.moveToFirst()) {
            val = c.isNull(0) ? 0.0 : c.getDouble(0);
        }
        c.close();
        return val;
    }

    public ArrayList<CategorySum> categoryBreakdownForMonth(int year, int month) {
        ArrayList<CategorySum> out = new ArrayList<>();
        String prefix = String.format(Locale.US, "%04d-%02d-", year, month);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT category, type, SUM(amount) AS s FROM " + TABLE + " WHERE date LIKE ? GROUP BY category, type ORDER BY s DESC", new String[]{prefix + "%"});
        while (c.moveToNext()) {
            String cat = c.getString(c.getColumnIndex("category"));
            String type = c.getString(c.getColumnIndex("type"));
            double s = c.getDouble(c.getColumnIndex("s"));
            out.add(new CategorySum(cat, type, s));
        }
        c.close();
        return out;
    }

    public Map<String, Double> getCategoryTotalsForMonth(int year, int month) {
        Map<String, Double> categoryTotals = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String datePattern = String.format(Locale.US, "%04d-%02d-%%", year, month);

        String query = "SELECT category, type, SUM(amount) FROM " + TABLE +
                " WHERE date LIKE ? GROUP BY category, type";

        Cursor cursor = db.rawQuery(query, new String[]{datePattern});

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                String type = cursor.getString(1);
                double total = cursor.getDouble(2);
                double amount = type.equals("INCOME") ? total : -total;
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryTotals;
    }

    // Keep legacy File method if needed, but added OutputStream method for SAF
    public File exportCsv(File targetFile) throws Exception {
        ArrayList<Expense> all = listAll();
        FileWriter fw = new FileWriter(targetFile);
        fw.write("date,timestamp,type,category,amount,note\n");
        for (Expense e : all) {
            String note = e.note == null ? "" : e.note.replace("\n", " ").replace(",", " ");
            String cat = e.category == null ? "" : e.category.replace(",", " ");
            String line = String.format(Locale.US, "%s,%d,%s,%s,%.2f,%s\n", e.date, e.timestamp, e.type, cat, e.amount, note);
            fw.write(line);
        }
        fw.flush();
        fw.close();
        return targetFile;
    }

    public void exportCsv(OutputStream out) throws Exception {
        ArrayList<Expense> all = listAll();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        bw.write("date,timestamp,type,category,amount,note\n");
        for (Expense e : all) {
            String note = e.note == null ? "" : e.note.replace("\n", " ").replace(",", " ");
            String cat = e.category == null ? "" : e.category.replace(",", " ");
            String line = String.format(Locale.US, "%s,%d,%s,%s,%.2f,%s\n", e.date, e.timestamp, e.type, cat, e.amount, note);
            bw.write(line);
        }
        bw.flush();
        bw.close();
    }

    // Keep legacy File method if needed
    public int importCsv(File srcFile) throws Exception {
        int count = 0;
        BufferedReader br = new BufferedReader(new FileReader(srcFile));
        String line = br.readLine(); // skip header
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        while ((line = br.readLine()) != null) {
            processLine(line, sdf);
            count++;
        }
        br.close();
        return count;
    }

    public int importCsv(InputStream in) throws Exception {
        int count = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine(); // skip header
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        while ((line = br.readLine()) != null) {
            processLine(line, sdf);
            count++;
        }
        br.close();
        return count;
    }

    private void processLine(String line, SimpleDateFormat sdf) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 6) return;
            String date = parts[0].trim();
            long timestamp;
            try {
                timestamp = Long.parseLong(parts[1].trim());
            } catch (Exception ex) {
                Date d = sdf.parse(date);
                timestamp = d != null ? d.getTime() : System.currentTimeMillis();
            }
            String type = parts[2].trim();
            String category = parts[3].trim();
            double amount = 0;
            try { amount = Double.parseDouble(parts[4].trim()); } catch (Exception ignored) {}
            String note = parts[5].trim();
            Expense e = new Expense(timestamp, date, type, category, amount, note);
            insert(e);
        } catch (Exception ignored) {}
    }

    public static class CategorySum {
        public String category;
        public String type;
        public double sum;

        public CategorySum(String category, String type, double sum) {
            this.category = category;
            this.type = type;
            this.sum = sum;
        }
    }
}
