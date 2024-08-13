package com.test.fincti

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter class for managing the list of transactions in a RecyclerView.
 *
 * @property transactions The list of transactions to display.
 */
class TransactionAdapter(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    /**
     * ViewHolder class for holding views related to each transaction item.
     *
     * @param view The view of the item in the RecyclerView.
     */
    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TextView for displaying the label of the transaction
        val label: TextView = view.findViewById(R.id.label)
        // TextView for displaying the amount of the transaction
        val amount: TextView = view.findViewById(R.id.amount)
    }

    /**
     * Creates a new ViewHolder for an item.
     *
     * @param parent The parent ViewGroup which will contain the new ViewHolder.
     * @param viewType The view type of the new ViewHolder.
     * @return A new instance of TransactionHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_layout, parent, false)
        return TransactionHolder(view)
    }

    /**
     * Binds data to the views in the ViewHolder.
     *
     * @param holder The ViewHolder which should be updated to reflect the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        // Get the transaction for the current position
        val transaction = transactions[position]
        val context = holder.amount.context

        // Format and set the amount text based on the transaction type
        if (transaction.type == "0") {
            holder.amount.text = "- $%.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red)) // Set color for expenses
        } else {
            holder.amount.text = "+ $%.2f".format(Math.abs(transaction.amount))
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green)) // Set color for income
        }

        // Set the label text
        holder.label.text = transaction.label

        // Set up a click listener to open a detailed activity with transaction details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailedActivity::class.java)
            intent.putExtra("transaction", transaction) // Pass the transaction data to the activity
            context.startActivity(intent) // Start the DetailedActivity
        }
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The size of the transactions list.
     */
    override fun getItemCount(): Int {
        return transactions.size
    }

    /**
     * Updates the data in the adapter and refreshes the view.
     *
     * @param transactions The new list of transactions to display.
     */
    fun setData(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }
}
