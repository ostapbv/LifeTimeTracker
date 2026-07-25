package com.example.lifetimetracker.data.repository

import com.example.lifetimetracker.data.local.dao.CategoryDao
import com.example.lifetimetracker.data.local.entity.CategoryEntity
import com.example.lifetimetracker.domain.model.Category
import com.example.lifetimetracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return dao.getAllCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertCategory(category: Category): Long {
        return dao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        dao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(categoryId: Long) {
        dao.deleteCategory(categoryId)
    }

    private fun CategoryEntity.toDomainModel(): Category {
        return Category(
            id = id,
            key = key,
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            dailyLimitMinutes = dailyLimitMinutes,
            isSystem = isSystem,
            sortOrder = sortOrder
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            key = key,
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            dailyLimitMinutes = dailyLimitMinutes,
            isSystem = isSystem,
            sortOrder = sortOrder,
            createdAt = System.currentTimeMillis(), // We might need a better way if updating, but for MVP it's okay
            updatedAt = System.currentTimeMillis()
        )
    }
}
