package com.example.testsehat.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.testsehat.databinding.ProfileFragmentBinding
import com.example.testsehat.AuthManager
import com.example.testsehat.LoginActivity

class ProfileFragment : Fragment() {
    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = AuthManager.getEmail(requireContext())
        email?.let {
            binding.tvUsername.text = it.split("@")[0] //
            binding.tvEmail.text = it
        }

        binding.btnLogout.setOnClickListener {
            AuthManager.logout(requireContext())
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}