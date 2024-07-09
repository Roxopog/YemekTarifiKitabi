package com.burak.yemektarifi2.view

import adaptor.TarifAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.burak.yemektarifi2.databinding.FragmentListeBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import model.Tarif
import roomdb.TarifDAO
import roomdb.tarifDataBase


class ListeFragment : Fragment() {

    private var _binding: FragmentListeBinding? = null
    private lateinit var db : tarifDataBase
    private lateinit var TarifDao : TarifDAO
    private val mDisposable = CompositeDisposable()
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(requireContext(), tarifDataBase::class.java,"tarifler").build()
        TarifDao = db.TarifDAO()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { YeniEkle(it) }
        binding.tarifRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        verilerial()
    }
    private fun verilerial(){
        mDisposable.add(TarifDao.getALl()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse))
    }

    private fun handleResponse(tarifler : List<Tarif>){
        val adapter = TarifAdapter(tarifler)
        binding.tarifRecyclerView.adapter = adapter
    }

    fun YeniEkle(view: View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "yeni", id=0)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}