package com.android.doctorAppointment;


import java.util.Arrays;

public class tests {
    public static void main(String[] args) {
        String[] values = {"Family Physician", "Internal Medicine Physician", "Pediatrician",
                "Obstetrician/Gynecologist (OB/GYN)", "Surgeon", "Psychiatrist", "Cardiologist",
                "Dermatologist", "Endocrinologist", "Gastroenterologist", "Infectious Disease " +
                "Physician", "Nephrologist", "Ophthalmologist", "Otolaryngologist", "Pulmonologist",
                "Neurologist","Physician Executive","Radiologist","Anesthesiologist","Oncologist"
        };

        Arrays.sort(values);
        for (String value:values) {
            System.out.println("<item>"+value+"</item>");
        }
    }
}
