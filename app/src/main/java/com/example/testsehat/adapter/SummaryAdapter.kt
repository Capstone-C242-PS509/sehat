package com.example.testsehat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testsehat.data.MentalDiseaseData
import com.example.testsehat.R

class SummaryAdapter(private var diseases: List<MentalDiseaseData> = listOf()) :
    RecyclerView.Adapter<SummaryAdapter.MentalDiseaseViewHolder>() {

    class MentalDiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diseaseNameTextView: TextView = itemView.findViewById(R.id.tvDiseaseName)
        private val diseaseCountTextView: TextView = itemView.findViewById(R.id.tvDiseaseCount)

        fun bind(disease: MentalDiseaseData) {
            diseaseNameTextView.text = disease.mental_disease
            diseaseCountTextView.text = "Count: ${disease.count}"
        }
    }

    fun updateDiseases(newDiseases: List<MentalDiseaseData>) {
        diseases = newDiseases
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentalDiseaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mental_disease, parent, false)
        return MentalDiseaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentalDiseaseViewHolder, position: Int) {
        holder.bind(diseases[position])
    }

    override fun getItemCount() = diseases.size
}