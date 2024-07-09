package adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.burak.yemektarifi2.databinding.RecyclerRowBinding
import com.burak.yemektarifi2.view.ListeFragmentDirections
import model.Tarif

class TarifAdapter(val tarifList: List<Tarif>) : RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    class TarifHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
            return tarifList.size

    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
        holder.binding.RecyclerViewTextView.text = tarifList[position].isim
        holder.itemView.setOnClickListener{
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("eski",id = tarifList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}