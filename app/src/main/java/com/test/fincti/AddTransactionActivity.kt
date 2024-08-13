package com.test.fincti

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var addTransactionBtn: Button
    private lateinit var closeBtn: AppCompatImageButton
    private lateinit var labelInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var typeInput: AutoCompleteTextView
    private lateinit var categoryInput: AutoCompleteTextView
    private lateinit var labelLayout: TextInputLayout
    private lateinit var amountLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var typeLayout: TextInputLayout
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var rootView: ConstraintLayout
    private lateinit var toolBar: MaterialToolbar
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imageView: ImageView
    private var typePosition: String = "0"
    private var categoryPosition: String = "0"
    private var photoByteArray: ByteArray? = null

    val types = arrayOf("Expense", "Income")
    val categories = arrayOf(
        "Food",
        "Rent",
        "Transportation",
        "Entertainment",
        "Salary",
        "Groceries",
        "Bill",
        "Maintenance",
        "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)


        labelLayout = findViewById(R.id.labelLayout)
        amountLayout = findViewById(R.id.amountLayout)
        descriptionLayout = findViewById(R.id.descriptionLayout)
        typeLayout = findViewById(R.id.typeLayout)
        categoryLayout = findViewById(R.id.categoryLayout)
        addTransactionBtn = findViewById(R.id.addTransactionBtn)
        closeBtn = findViewById(R.id.closeBtn)
        labelInput = findViewById(R.id.labelInput)
        amountInput = findViewById(R.id.amountInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        typeInput = findViewById(R.id.typeInput)
        categoryInput = findViewById(R.id.categoryInput)
        rootView = findViewById(R.id.rootView)
        toolBar = findViewById(R.id.toolBar)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawerLayout)
        imageView = findViewById(R.id.imageView)

        val typeAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types)
        val categoryAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories)

        typeInput.setAdapter(typeAdapter)
        categoryInput.setAdapter(categoryAdapter)

        typeInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                typePosition = position.toString()
            }

        categoryInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                categoryPosition = position.toString()
            }

        labelInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                amountLayout.error = null
        }

        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

        addTransactionBtn.setOnClickListener {
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val description = descriptionInput.text.toString()
            val type = typePosition
            val category = categoryPosition

            if (label.isEmpty()) {
                labelLayout.error = "Please enter a valid label"
            } else if (amount == null) {
                amountLayout.error = "Please enter a valid amount"
            } else if (photoByteArray == null) {
                // Show an error message if no photo is selected
                Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            } else {
                val transaction = Transaction(
                    0,
                    label,
                    amount,
                    description,
                    type,
                    category,
                    photoByteArray!!
                )

                GlobalScope.launch {
                    val db = Room.databaseBuilder(
                        this@AddTransactionActivity,
                        AppDatabase::class.java,
                        "transactions"
                    ).build()
                    db.transactionDao().insertAll(transaction)
                    finish()
                }
            }
        }

        closeBtn.setOnClickListener {
            finish()
        }

        rootView.setOnClickListener {
            this.window.decorView.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
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
                    // Handle list button click
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
                    // Handle list button click
                    val intent = Intent(this, ChallengeActivity::class.java)
                    startActivity(intent)
                    true
                }
                // Add more menu item handlers as needed
            }
            false
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            photoByteArray = byteArrayOutputStream.toByteArray()
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }
}