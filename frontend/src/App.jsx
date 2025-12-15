import React, { useEffect, useState } from 'react'

function totalsByCategory(items) {
  const map = {}
  for (const e of items) {
    const cat = e.category || 'Uncategorized'
    map[cat] = (map[cat] || 0) + Number(e.amount || 0)
  }
  return map
}

const API_BASE = 'http://localhost:8080/api/expenses'

export default function App() {
  const [items, setItems] = useState([])
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('Food')
  const [amount, setAmount] = useState('')
  const [amountError, setAmountError] = useState('')
  const [toast, setToast] = useState('')
  const [editingItem, setEditingItem] = useState(null)
  const [editDescription, setEditDescription] = useState('')
  const [editCategory, setEditCategory] = useState('Food')
  const [editAmount, setEditAmount] = useState('')

  useEffect(() => {
    fetch(API_BASE)
      .then(r => r.json())
      .then(data => setItems(data))
      .catch(() => setItems([]))
  }, [])

  function isValidAmount(v) {
    if (!v && v !== '0') return false
    const s = String(v).trim()
    const pattern = /^\d+(\.\d{1,2})?$|^\.\d{1,2}$/
    if (!pattern.test(s)) return false
    const num = Number(s)
    if (!isFinite(num)) return false
    return num >= 0
  }

  function showToast(msg) {
    setToast(msg)
    setTimeout(() => setToast(''), 3000)
  }

  function add() {
    if (!description) return
    const val = (amount || '').trim()
    if (!isValidAmount(val)) {
      const msg = 'Enter a non-negative number with up to 2 decimal places'
      setAmountError(msg)
      showToast(msg)
      return
    }
    setAmountError('')
    const payload = { description, category, amount: parseFloat(val) }
    fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(r => r.json())
      .then(created => {
        setItems(prev => [...prev, created])
        setDescription('')
        setAmount('')
      })
      .catch(() => alert('Failed to create expense'))
  }

  function remove(id) {
    fetch(API_BASE + '/' + id, { method: 'DELETE' })
      .then(r => {
        if (r.status === 204) setItems(prev => prev.filter(i => i.id !== id))
      })
      .catch(() => alert('Failed to delete'))
  }

  function edit(id) {
    const original = items.find(i => i.id === id)
    if (!original) return
    setEditingItem(id)
    setEditDescription(original.description)
    setEditCategory(original.category || 'Uncategorized')
    setEditAmount(String(original.amount))
    setAmountError('')
  }

  function cancelEdit() {
    setEditingItem(null)
    setEditDescription('')
    setEditCategory('Food')
    setEditAmount('')
    setAmountError('')
  }

  function saveEdit() {
    if (!editingItem) return
    if (!isValidAmount(editAmount)) {
      const msg = 'Invalid amount: must be non-negative and have at most 2 decimals'
      setAmountError(msg)
      showToast(msg)
      return
    }
    const payload = { description: editDescription, category: editCategory, amount: parseFloat(editAmount) }
    fetch(API_BASE + '/' + editingItem, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(r => {
        if (r.status === 204) {
          setItems(prev => prev.map(i => i.id === editingItem ? { ...i, description: editDescription, category: editCategory, amount: parseFloat(editAmount).toFixed(2) } : i))
          cancelEdit()
        } else {
          showToast('Failed to save changes')
        }
      })
      .catch(() => showToast('Failed to save changes'))
  }

  function onAmountChange(v) {
    // Only update the input during typing. Validate on submit (Add button).
    setAmount(v)
    // clear any previous error while typing
    setAmountError('')
  }

  const totals = totalsByCategory(items)

  return (
    <div className="container">
      <h1>Expense Tracker</h1>

      <div className="form">
        <input placeholder="Description" value={description} onChange={e => setDescription(e.target.value)} />
        <input placeholder="Amount" value={amount} onChange={e => onAmountChange(e.target.value)} />
        <select value={category} onChange={e => setCategory(e.target.value)}>
          <option>Food</option>
          <option>House</option>
          <option>Hobby</option>
          <option>Unexpected</option>
          <option>Other</option>
        </select>
        <button onClick={add}>Add</button>
      </div>

      <h2>Entries</h2>
      <ul className="list">
        {items.map(it => (
          <li key={it.id}>
            <div className="line">
              <div>{it.description} — <b>{it.category}</b> — ${it.amount}</div>
              <div className="actions">
                <button onClick={() => edit(it.id)}>Edit</button>
                <button onClick={() => remove(it.id)}>Delete</button>
              </div>
            </div>
          </li>
        ))}
      </ul>

      <h2>Totals by category</h2>
      <ul>
        {Object.entries(totals).map(([k, v]) => (
          <li key={k}>{k}: ${Number(v).toFixed(2)}</li>
        ))}
      </ul>

      {toast && (
        <div className="toast">{toast}</div>
      )}

      {editingItem && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Edit expense</h3>
            <div className="modal-row">
              <label>Description</label>
              <input value={editDescription} onChange={e => setEditDescription(e.target.value)} />
            </div>
            <div className="modal-row">
              <label>Category</label>
              <select value={editCategory} onChange={e => setEditCategory(e.target.value)}>
                <option>Food</option>
                <option>House</option>
                <option>Hobby</option>
                <option>Unexpected</option>
                <option>Other</option>
                <option>Uncategorized</option>
              </select>
            </div>
            <div className="modal-row">
              <label>Amount</label>
              <input value={editAmount} onChange={e => setEditAmount(e.target.value)} />
            </div>
            {amountError && <div className="modal-error">{amountError}</div>}
            <div className="modal-actions">
              <button onClick={saveEdit}>Save</button>
              <button onClick={cancelEdit}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
