package com.example.adoptionproject;

import com.example.adoptionproject.entities.*;
import com.example.adoptionproject.repositories.*;
import com.example.adoptionproject.services.AdoptionServicesImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionServicesImplTest {

    @Mock
    private AdoptantRepository adoptantRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AdoptionRepository adoptionRepository;

    @InjectMocks
    private AdoptionServicesImpl adoptionServices;

    @Test
    void addAdoptant_ShouldSaveAndReturnAdoptant() {
        // Given
        Adoptant adoptant = new Adoptant();
        adoptant.setIdAdoptant(1);
        adoptant.setNom("Mohamed");
        adoptant.setAdresse("123 Rue Test");
        adoptant.setTelephone("0123456789");

        when(adoptantRepository.save(any(Adoptant.class))).thenReturn(adoptant);

        // When
        Adoptant result = adoptionServices.addAdoptant(adoptant);

        // Then
        assertNotNull(result);
        assertEquals("Dupont", result.getNom());
        verify(adoptantRepository, times(1)).save(adoptant);
    }

    @Test
    void addAnimal_ShouldSaveAndReturnAnimal() {
        // Given
        Animal animal = new Animal();
        animal.setIdAnimal(1);
        animal.setNom("Médor");
        animal.setAge(3);
        animal.setSterilise(true);
        animal.setEspece(Espece.CHIEN);

        when(animalRepository.save(any(Animal.class))).thenReturn(animal);

        // When
        Animal result = adoptionServices.addAnimal(animal);

        // Then
        assertNotNull(result);
        assertEquals("Médor", result.getNom());
        assertEquals(Espece.CHIEN, result.getEspece());
        verify(animalRepository, times(1)).save(animal);
    }

    @Test
    void addAdoption_ShouldSaveAdoption_WhenAdoptantAndAnimalExist() {
        // Given
        Adoptant adoptant = new Adoptant();
        adoptant.setIdAdoptant(1);
        adoptant.setNom("Dupont");

        Animal animal = new Animal();
        animal.setIdAnimal(1);
        animal.setNom("Médor");

        Adoption adoption = new Adoption();
        adoption.setIdAdoption(1);
        adoption.setFrais(150.0f);

        when(adoptantRepository.findById(1)).thenReturn(Optional.of(adoptant));
        when(animalRepository.findById(1)).thenReturn(Optional.of(animal));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(adoption);

        // When
        Adoption result = adoptionServices.addAdoption(adoption, 1, 1);

        // Then
        assertNotNull(result);
        assertEquals(150.0f, result.getFrais());
        assertEquals(adoptant, result.getAdoptant());
        assertEquals(animal, result.getAnimal());
        verify(adoptionRepository, times(1)).save(adoption);
    }

    @Test
    void addAdoption_ShouldReturnNull_WhenAdoptantNotFound() {
        // Given
        Adoption adoption = new Adoption();
        when(adoptantRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Adoption result = adoptionServices.addAdoption(adoption, 1, 1);

        // Then
        assertNull(result);
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    @Test
    void addAdoption_ShouldReturnNull_WhenAnimalNotFound() {
        // Given
        Adoptant adoptant = new Adoptant();
        adoptant.setIdAdoptant(1);

        Adoption adoption = new Adoption();

        when(adoptantRepository.findById(1)).thenReturn(Optional.of(adoptant));
        when(animalRepository.findById(1)).thenReturn(Optional.empty());

        // When
        Adoption result = adoptionServices.addAdoption(adoption, 1, 1);

        // Then
        assertNull(result);
        verify(adoptionRepository, never()).save(any(Adoption.class));
    }

    @Test
    void getAdoptionsByAdoptant_ShouldReturnAdoptionsList() {
        // Given
        Adoptant adoptant = new Adoptant();
        adoptant.setNom("Dupont");

        Adoption adoption1 = new Adoption();
        adoption1.setIdAdoption(1);
        adoption1.setFrais(100.0f);
        adoption1.setAdoptant(adoptant);

        Adoption adoption2 = new Adoption();
        adoption2.setIdAdoption(2);
        adoption2.setFrais(200.0f);
        adoption2.setAdoptant(adoptant);

        List<Adoption> adoptions = Arrays.asList(adoption1, adoption2);

        when(adoptionRepository.findByAdoptant_Nom("Dupont")).thenReturn(adoptions);

        // When
        List<Adoption> result = adoptionServices.getAdoptionsByAdoptant("Dupont");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100.0f, result.get(0).getFrais());
        verify(adoptionRepository, times(1)).findByAdoptant_Nom("Dupont");
    }

    @Test
    void calculFraisTotalAdoptions_ShouldReturnCorrectSum() {
        // Given
        Adoptant adoptant = new Adoptant();
        adoptant.setIdAdoptant(1);

        Adoption adoption1 = new Adoption();
        adoption1.setIdAdoption(1);
        adoption1.setFrais(100.0f);
        adoption1.setAdoptant(adoptant);

        Adoption adoption2 = new Adoption();
        adoption2.setIdAdoption(2);
        adoption2.setFrais(200.0f);
        adoption2.setAdoptant(adoptant);

        List<Adoption> adoptions = Arrays.asList(adoption1, adoption2);

        when(adoptionRepository.findByAdoptant_IdAdoptant(1)).thenReturn(adoptions);

        // When
        float result = adoptionServices.calculFraisTotalAdoptions(1);

        // Then
        assertEquals(300.0f, result);
        verify(adoptionRepository, times(1)).findByAdoptant_IdAdoptant(1);
    }

    @Test
    void calculFraisTotalAdoptions_ShouldReturnZero_WhenNoAdoptions() {
        // Given
        when(adoptionRepository.findByAdoptant_IdAdoptant(1)).thenReturn(Arrays.asList());

        // When
        float result = adoptionServices.calculFraisTotalAdoptions(1);

        // Then
        assertEquals(0.0f, result);
        verify(adoptionRepository, times(1)).findByAdoptant_IdAdoptant(1);
    }
}