package com.example.seekshakcom.ui.myaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.seekshakcom.databinding.FragmentMyaccountBinding

class MyAccountFragment : Fragment() {

    // ViewBinding variable
    private var _binding: FragmentMyaccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyaccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.userId.text = "Std-1234"
        binding.userName.text = "Sandeep Kumar"
        binding.mobileNo.text = "+91 9876543210"
        binding.userEmail.text = "sandeepkumar9334@gmail.com"
    }

    private fun setupListeners() {
        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.icCamera.setOnClickListener {
            Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
