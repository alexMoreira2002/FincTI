package com.test.fincti

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.test.fincti.AppDatabase

/**
 * Main activity that displays a list of transactions and manages user interactions.
 */
class MainActivity : AppCompatActivity() {

    // Variables for managing the state of transactions and UI elements
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearlayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var balanceTxt: TextView
    private lateinit var incomeTxt: TextView
    private lateinit var expenseTxt: TextView
    private lateinit var addbtn: FloatingActionButton
    private lateinit var db: AppDatabase
    private lateinit var toolBar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    /**
     * Called when the activity is first created.
     * Sets up the UI elements, initializes the database, and sets up event listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerView)
        balanceTxt = findViewById(R.id.balance)
        incomeTxt = findViewById(R.id.income)
        expenseTxt = findViewById(R.id.expense)
        addbtn = findViewById(R.id.addTransaction) as FloatingActionButton
        toolBar = findViewById(R.id.tollBar)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up the toolbar
        setSupportActionBar(toolBar)

        // Initialize the transaction list and adapter
        transactions = arrayListOf()
        transactionAdapter = TransactionAdapter(transactions)
        linearlayoutManager = LinearLayoutManager(this)

        // Initialize the Room database
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        // Set up the RecyclerView with the adapter and layout manager
        recyclerView.apply {
            adapter = transactionAdapter
            layoutManager = linearlayoutManager
        }

        // Set up item swipe to delete functionality
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

        // Set up the FloatingActionButton to navigate to AddTransactionActivity
        addbtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        // Set up the NavigationView's item selection listener
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home button click
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_add -> {
                    // Handle add button click
                    val intent = Intent(this, AddTransactionActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_list -> {
                    // Handle list button click
                    val intent = Intent(this, ListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_challenge -> {
                    // Handle challenge button click
                    val intent = Intent(this, ChallengeActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Add more menu item handlers as needed
                else -> false
            }
        }

        // Set up the ActionBarDrawerToggle for drawer layout
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolBar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    /**
     * Fetches all transactions from the database and updates the UI.
     */
    private fun fetchAll() {
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    /**
     * Updates the dashboard UI with the current balance, income, and expenses.
     */
    private fun updateDashboard() {
        val expenseAmount = transactions.filter { it.type == "0" }.map { it.amount }.sum()
        val incomeAmount = transactions.filter { it.type == "1" }.map { it.amount }.sum()
        val totalAmount = incomeAmount - expenseAmount

        balanceTxt.text = "$ %.2f".format(totalAmount)
        incomeTxt.text = "$ %.2f".format(incomeAmount)
        expenseTxt.text = "$ %.2f".format(expenseAmount)

        if (totalAmount < 0) {
            Toast.makeText(this, "Warning: Your balance is negative!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Deletes a transaction and shows a Snackbar with an undo option.
     *
     * @param transaction The transaction to be deleted.
     */
    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    /**
     * Undoes the last delete operation by re-inserting the deleted transaction.
     */
    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    /**
     * Displays a Snackbar with an undo option for the deleted transaction.
     */
    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    /**
     * Called when the activity is resumed. Fetches the latest transactions.
     */
    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}
