package com.test.fincti

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class DetailedActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var transaction: Transaction
    private lateinit var closeBtn: ImageButton
    private lateinit var labelInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var typeInput: AutoCompleteTextView
    private lateinit var categoryInput: AutoCompleteTextView
    private lateinit var labelLayout: TextInputLayout
    private lateinit var amountLayout: TextInputLayout
    private lateinit var typeLayout: TextInputLayout
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var rootView: ConstraintLayout
    private lateinit var updateBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var toolBar: MaterialToolbar
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private var typePosition: String = "0"
    private var categoryPosition: String = "0"
    private lateinit var imageView: ImageView

    // Arrays for dropdown options
    val types = arrayOf("Expense", "Income")
    val categories = arrayOf(
        "Food", "Rent", "Transportation", "Entertainment",
        "Salary", "Groceries", "Bill", "Maintenance", "Others"
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display mode
        setContentView(R.layout.activity_detailed) // Set the layout for this activity

        // Initialize UI elements
        closeBtn = findViewById(R.id.closeBtn)
        labelInput = findViewById(R.id.labelInput)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        typeInput = findViewById(R.id.typeInput)
        categoryInput = findViewById(R.id.categoryInput)
        labelLayout = findViewById(R.id.labelLayout)
        amountLayout = findViewById(R.id.amountLayout)
        typeLayout = findViewById(R.id.typeLayout)
        categoryLayout = findViewById(R.id.categoryLayout)
        rootView = findViewById(R.id.rootView)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        toolBar = findViewById(R.id.toolBar)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawerLayout)
        imageView = findViewById(R.id.imageView)

        // Retrieve the transaction object from the Intent
        transaction = intent.getSerializableExtra("transaction") as Transaction

        // Populate fields with transaction data
        labelInput.setText(transaction.label)
        amountInput.setText(transaction.amount.toString())
        descriptionInput.setText(transaction.description)

        // Set the initial values for type and category based on transaction data
        when (transaction.type) {
            "0" -> typeInput.setText(types[0])
            "1" -> typeInput.setText(types[1])
            else -> typeInput.setText("") // Default case if type is invalid
        }

        when (transaction.category) {
            "0" -> categoryInput.setText(categories[0])
            "1" -> categoryInput.setText(categories[1])
            "2" -> categoryInput.setText(categories[2])
            "3" -> categoryInput.setText(categories[3])
            "4" -> categoryInput.setText(categories[4])
            "5" -> categoryInput.setText(categories[5])
            "6" -> categoryInput.setText(categories[6])
            "7" -> categoryInput.setText(categories[7])
            "8" -> categoryInput.setText(categories[8])
            else -> categoryInput.setText("") // Default case if category is invalid
        }

        // Set positions based on type and category
        typePosition = when (transaction.type) {
            "0" -> "0"
            "1" -> "1"
            else -> ""
        }

        categoryPosition = when (transaction.category) {
            "0" -> "0"
            "1" -> "1"
            "2" -> "2"
            "3" -> "3"
            "4" -> "4"
            "5" -> "5"
            "6" -> "6"
            "7" -> "7"
            "8" -> "8"
            else -> ""
        }

        // Set up adapters for type and category dropdowns
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        typeInput.setAdapter(typeAdapter)
        categoryInput.setAdapter(categoryAdapter)

        // Load and display the photo if available
        if (transaction.photo != null) {
            val byteArray = transaction.photo
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
            imageView.setImageBitmap(bitmap)
        }

        // Open gallery to pick a new photo
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

        // Hide keyboard when clicking outside of input fields
        rootView.setOnClickListener {
            this.window.decorView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        // Update button visibility logic
        labelInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
            if (it!!.count() > 0) labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
            if (it!!.count() > 0) amountLayout.error = null
        }

        descriptionInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
        }

        typeInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
        }

        categoryInput.addTextChangedListener {
            updateBtn.visibility = View.VISIBLE
        }

        // Set listeners for type and category selection
        typeInput.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            typePosition = position.toString()
        }

        categoryInput.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            categoryPosition = position.toString()
        }

        // Handle update button click
        updateBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val description = descriptionInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            if (label.isEmpty()) labelLayout.error = "Please enter a valid label"
            else if (amount == null) amountLayout.error = "Please enter a valid amount"
            else {
                // Convert image to byte array
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val photoByteArray = byteArrayOutputStream.toByteArray()

                // Create a new transaction object with updated data
                val updatedTransaction = Transaction(transaction.id, label, amount, description, typePosition, categoryPosition, photoByteArray)
                GlobalScope.launch {
                    val db = Room.databaseBuilder(
                        this@DetailedActivity, AppDatabase::class.java, "transactions"
                    ).build()
                    db.transactionDao().update(updatedTransaction)
                    finish()
                }
            }
        }

        // Handle delete button click
        deleteBtn.setOnClickListener {
            val transactionId = transaction.id
            val db = Room.databaseBuilder(
                this, AppDatabase::class.java, "transactions"
            ).build()

            // Show confirmation dialog before deletion
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Delete Transaction")
            alertDialog.setMessage("Are you sure you want to delete this transaction?")
            alertDialog.setPositiveButton("Yes") { _, _ ->
                GlobalScope.launch {
                    db.transactionDao().delete(transaction)
                    finish()
                }
            }
            alertDialog.setNegativeButton("No") { _, _ -> }
            alertDialog.show()
        }

        // Handle close button click
        closeBtn.setOnClickListener {
            finish() // Close the activity
        }

        // Set up toolbar and navigation drawer
        setSupportActionBar(toolBar)
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
            }
            false
        }

        // Set up the drawer toggle
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

    // Function to update a transaction in the database (not used in this activity, but included for completeness)
    private fun update(transaction: Transaction) {
        val db = Room.databaseBuilder(
            this, AppDatabase::class.java, "transactions"
        ).build()

        GlobalScope.launch {
            db.transactionDao().update(transaction)
            finish() // Close the activity after updating the transaction
        }
    }
}
