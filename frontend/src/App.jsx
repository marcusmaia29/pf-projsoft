import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const API = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');

export default function App() {
  const { isLoading, isAuthenticated, user, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0();
  const [artigos, setArtigos] = useState([]);
  const [admin, setAdmin] = useState(false);
  const [titulo, setTitulo] = useState('');
  const [descricao, setDescricao] = useState('');
  const [prioridade, setPrioridade] = useState('Média');
  const [erro, setErro] = useState('');

  async function chamar(path, method = 'GET', body) {
    const token = await getAccessTokenSilently();
    const dados = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const roles = dados['https://social-insper.com/roles'] || [];
    setAdmin(roles.includes('ADMIN'));

    const response = await fetch(API + path, {
      method,
      headers: { Authorization: 'Bearer ' + token, 'Content-Type': 'application/json' },
      body: body ? JSON.stringify(body) : undefined,
    });
    if (!response.ok) throw new Error('Erro ' + response.status);
    return response.status === 204 ? null : response.json();
  }

  async function listar() {
    setErro('');
    try {
      setArtigos(await chamar('/artigos'));
    } catch (e) {
      setErro(e.message);
    }
  }

  async function criar(event) {
    event.preventDefault();
    setErro('');
    try {
      await chamar('/artigos', 'POST', { titulo, descricao, prioridade });
      setTitulo('');
      setDescricao('');
      listar();
    } catch (e) {
      setErro(e.message);
    }
  }

  async function deletar(id) {
    setErro('');
    try {
      await chamar('/artigos/' + id, 'DELETE');
      listar();
    } catch (e) {
      setErro(e.message);
    }
  }

  useEffect(() => {
    if (isAuthenticated) listar();
  }, [isAuthenticated]);

  if (isLoading) return <p>Carregando...</p>;

  if (!isAuthenticated) {
    return (
      <div className="container">
        <h1>Base de conhecimentos</h1>
        <button onClick={() => loginWithRedirect()}>Entrar</button>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>Base de conhecimentos</h1>
      <p>
        {user?.email} ({admin ? 'admin' : 'usuário'}){' '}
        <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>Sair</button>
      </p>

      {erro && <p className="erro">{erro}</p>}

      {admin && (
        <form onSubmit={criar}>
          <h2>Novo artigo</h2>
          <input placeholder="Título" value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
          <textarea placeholder="Descrição" value={descricao} onChange={(e) => setDescricao(e.target.value)} required />
          <select value={prioridade} onChange={(e) => setPrioridade(e.target.value)}>
            <option>Alta</option>
            <option>Média</option>
            <option>Baixa</option>
          </select>
          <button type="submit">Criar</button>
        </form>
      )}

      <h2>Artigos</h2>
      {artigos.length === 0 && <p>Nenhum artigo cadastrado.</p>}
      <table>
        <thead>
          <tr>
            <th>Título</th>
            <th>Descrição</th>
            <th>Prioridade</th>
            <th>Criado por</th>
            {admin && <th></th>}
          </tr>
        </thead>
        <tbody>
          {artigos.map((artigo) => (
            <tr key={artigo.id}>
              <td>{artigo.titulo}</td>
              <td>{artigo.descricao}</td>
              <td>{artigo.prioridade}</td>
              <td>{artigo.emailCriador}</td>
              {admin && (
                <td>
                  <button onClick={() => deletar(artigo.id)}>Deletar</button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
