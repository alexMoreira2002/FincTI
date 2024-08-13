package com.test.fincti

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
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

class ChallengeActivity : AppCompatActivity() {
    // UI Elements
    private lateinit var challengeBtn: Button
    private lateinit var closeBtn: AppCompatImageButton
    private lateinit var incrementInput: TextInputEditText
    private lateinit var incrementLayout: TextInputLayout
    private lateinit var rootView: ConstraintLayout
    private lateinit var toolBar: MaterialToolbar
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display mode
        setContentView(R.layout.activity_challenge) // Set the layout for this activity

        // Initialize UI elements
        incrementLayout = findViewById(R.id.incrementLayout)
        challengeBtn = findViewById(R.id.challengeBtn)
        closeBtn = findViewById(R.id.closeBtn)
        incrementInput = findViewById(R.id.incrementInput)
        rootView = findViewById(R.id.rootView)
        toolBar = findViewById(R.id.toolBar)
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawerLayout)

        // Add a text change listener to clear error message when input is not empty
        incrementInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                incrementLayout.error = null
        }

        // Handle challenge button click
        challengeBtn.setOnClickListener {
            val increment = incrementInput.text.toString()

            if (increment.isEmpty()) {
                incrementLayout.error = "Please enter a valid increment amount"
            } else {
                val incrementValue = increment.toIntOrNull()

                if (incrementValue == null) {
                    incrementLayout.error = "Please enter a valid increment amount"
                    return@setOnClickListener
                }

                // Calculate the total amount saved over 52 weeks
                var totalChallenge: Int = 0
                var count = 0
                do {
                    totalChallenge += incrementValue * count
                    count++
                } while (count <= 52)

                // Show the result in an alert dialog
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("52 Week Challenge")
                alertDialog.setMessage("You would save a total of $$totalChallenge")
                alertDialog.setNeutralButton("OK") { _, _ -> }
                alertDialog.show()
            }
        }

        // Handle close button click
        closeBtn.setOnClickListener {
            finish() // Close the activity
        }

        // Hide keyboard when clicking outside of input fields
        rootView.setOnClickListener {
            this.window.decorView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        // Set up the toolbar and navigation drawer
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

    // Function to insert a transaction into the database (not used in this activity, but included for completeness)
    private fun insert(transaction: Transaction) {
        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish() // Close the activity after inserting the transaction
        }
    }
}
