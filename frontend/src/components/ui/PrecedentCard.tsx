import { useState } from 'react';
import { PrecedentMatch } from '../../types';
import SimilarityBadge from './SimilarityBadge';
import { Heart, Scale, Calendar, Building2, ChevronDown, ChevronUp } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { favoritesApi } from '../../api/favorites';

interface PrecedentCardProps {
  match: PrecedentMatch;
  documentId: number;
}

export default function PrecedentCard({ match, documentId }: PrecedentCardProps) {
  const [expanded, setExpanded] = useState(false);
  const [isFav, setIsFav] = useState(match.isFavorite);
  const queryClient = useQueryClient();

  const addFav = useMutation({
    mutationFn: () => favoritesApi.add(match.precedentId),
    onSuccess: () => {
      setIsFav(true);
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });

  const removeFav = useMutation({
    mutationFn: () => favoritesApi.remove(match.precedentId),
    onSuccess: () => {
      setIsFav(false);
      queryClient.invalidateQueries({ queryKey: ['favorites'] });
    },
  });

  const handleFavToggle = () => {
    if (isFav) {
      removeFav.mutate();
    } else {
      addFav.mutate();
    }
  };

  return (
    <div className="card hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2 flex-wrap">
            <SimilarityBadge score={match.similarityScore} />
            {match.caseNumber && (
              <span className="badge badge-gray">
                <Scale className="w-3 h-3 mr-1" />
                {match.caseNumber}
              </span>
            )}
            {match.category && (
              <span className="badge badge-blue">{match.category.name}</span>
            )}
          </div>

          <h3 className="text-base font-semibold text-gray-900 mb-2 leading-snug">
            {match.title}
          </h3>

          <div className="flex items-center gap-4 text-xs text-gray-500 mb-3">
            {match.courtName && (
              <span className="flex items-center gap-1">
                <Building2 className="w-3 h-3" />
                {match.courtName}
              </span>
            )}
            {match.decisionDate && (
              <span className="flex items-center gap-1">
                <Calendar className="w-3 h-3" />
                {new Date(match.decisionDate).toLocaleDateString('ru-RU')}
              </span>
            )}
          </div>

          {match.summary && (
            <p className="text-sm text-gray-600 mb-3 line-clamp-3">{match.summary}</p>
          )}

          {match.decision && (
            <div className="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded font-medium">
              Решение: {match.decision}
            </div>
          )}

          {match.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-3">
              {match.tags.map((tag) => (
                <span
                  key={tag.id}
                  className="px-2 py-0.5 rounded-full text-xs font-medium text-white"
                  style={{ backgroundColor: tag.color || '#6b7280' }}
                >
                  {tag.name}
                </span>
              ))}
            </div>
          )}
        </div>

        <button
          onClick={handleFavToggle}
          disabled={addFav.isPending || removeFav.isPending}
          className={`flex-shrink-0 p-2 rounded-lg transition-colors ${
            isFav
              ? 'text-red-500 bg-red-50 hover:bg-red-100'
              : 'text-gray-400 bg-gray-50 hover:bg-gray-100'
          }`}
          title={isFav ? 'Убрать из избранного' : 'Добавить в избранное'}
        >
          <Heart className={`w-4 h-4 ${isFav ? 'fill-current' : ''}`} />
        </button>
      </div>
    </div>
  );
}
