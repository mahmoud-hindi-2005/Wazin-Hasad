package com.hindi.wazinhasad;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList; // القائمة
    private OnTransactionClickListener listener; // مستمع النقر على العناصر

    public void updateCurrency(String symbolOnly) {
    }

    public interface OnTransactionClickListener {
        void onDeleteClick(Transaction transaction); // الحذف
        void onItemClick(Transaction transaction); // النقر على العنصر
    }

    public TransactionAdapter(List<Transaction> transactionList, OnTransactionClickListener listener) {
        this.transactionList = transactionList;
        this.listener = listener;
    }

    // تحويل التصميم لكود جافا
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    // ربط البيانات بالعناصر

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(transaction.getDate());

        double amount = transaction.getAmount();
        String currency = transaction.getCurrency();

        if ("income".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setText("+" + String.format("%.2f", amount) + " " + currency);
            holder.tvAmount.setTextColor(Color.parseColor("#2ECC71"));
        } else {
            holder.tvAmount.setText("-" + String.format("%.2f", amount) + " " + currency);
            holder.tvAmount.setTextColor(Color.parseColor("#E74C3C"));
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(transaction));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(transaction));
    }

    @Override
    public int getItemCount() { return transactionList.size(); }  // حجم وعدد العناصر

    // تحديث القائمة
    public void updateList(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    // حفظ العناصر لمرة واحدة من خلال المتغيرات
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}