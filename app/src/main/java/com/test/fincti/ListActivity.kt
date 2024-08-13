package com.test.fincti

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var typeLayout: TextInputLayout
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var typeInput: AutoCompleteTextView
    private lateinit var categoryInput: AutoCompleteTextView
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearlayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase
    private var currentTypeFilter: String? = null
    private var currentCategoryFilter: String? = null
    private lateinit var toolBar: MaterialToolbar
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    // Dropdown options for type and category
    val types = arrayOf("Expense", "Income")
    val categories = arrayOf(
        "Food",
        "Rent",
        "Transportation",
        "Entertainment",
        "Salary",
        "Groceries",
        "Bill",
        "Maintenance"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display mode
        setContentView(R.layout.activity_list) // Set the layout for this activity

        // Initialize UI elements
        typeInput = findViewById(R.id.typeInput)
        categoryInput = findViewById(R.id.categoryInput)
        typeLayout = findViewById(R.id.typeLayout)
        categoryLayout = findViewById(R.id.categoryLayout)
        recyclerView = findViewById(R.id.recyclerView)
        transactions = arrayListOf()
        transactionAdapter = TransactionAdapter(transactions)
        linearlayoutManager = LinearLayoutManager(this)
        toolBar = findViewById(R.id.toolBar)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawerLayout)

        // Set up adapters for the dropdown fields
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        typeInput.setAdapter(typeAdapter)
        categoryInput.setAdapter(categoryAdapter)

        // Set up item click listeners for the dropdown fields
        typeInput.setOnItemClickListener { _, _, position, _ ->
            currentTypeFilter = position.toString()
            fetchAll() // Fetch transactions based on the selected type
        }

        categoryInput.setOnItemClickListener { _, _, position, _ ->
            currentCategoryFilter = position.toString()
            fetchAll() // Fetch transactions based on the selected category
        }

        // Initialize the Room database
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        // Set up the RecyclerView
        recyclerView.apply {
            adapter = transactionAdapter
            layoutManager = linearlayoutManager
        }

        // Set up swipe-to-delete functionality
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

        // Attach ItemTouchHelper to RecyclerView
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

        // Set up the toolbar and navigation drawer
        setSupportActionBar(toolBar)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to the Home activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_add -> {
                    // Navigate to the Add Transaction activity
                    val intent = Intent(this, AddTransactionActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_list -> {
                    // Navigate to the List activity
                    val intent = Intent(this, ListActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Add more menu item handlers as needed
            }
            false
        }

        // Set up the DrawerToggle for the navigation drawer
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

    // Fetch transactions from the database based on filters
    private fun fetchAll() {
        GlobalScope.launch {
            val transactions = when {
                currentTypeFilter != null && currentCategoryFilter != null -> {
                    db.transactionDao().getAllByTypeAndCategory(currentTypeFilter!!, currentCategoryFilter!!)
                }
                currentTypeFilter != null -> {
                    db.transactionDao().getAllByType(currentTypeFilter!!)
                }
                currentCategoryFilter != null -> {
                    db.transactionDao().getAllByCategory(currentCategoryFilter!!)
                }
                else -> {
                    db.transactionDao().getAll()
                }
            }

            // Update the RecyclerView adapter with fetched data
            runOnUiThread {
                transactionAdapter.setData(transactions)
            }
        }
    }

    // Delete a transaction from the database and update the RecyclerView
    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                transactionAdapter.setData(transactions)
                showSnackbar() // Show a Snackbar with an undo option
            }
        }
    }

    // Undo the deletion of a transaction
    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            runOnUiThread {
                transactionAdapter.setData(transactions)
            }
        }
    }

    // Show a Snackbar with an undo option
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

    // Fetch transactions when the activity resumes
    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}
