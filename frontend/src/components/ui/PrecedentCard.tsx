import { useState } from 'react';
import { PrecedentMatch } from '../../types';
import SimilarityBadge from './SimilarityBadge';
import { Scale, Calendar, Building2, ChevronDown, ChevronUp } from 'lucide-react';

interface PrecedentCardProps {
  match: PrecedentMatch;
}

const SUMMARY_PREVIEW_LIMIT = 260;

export default function PrecedentCard({ match }: PrecedentCardProps) {
  const [expanded, setExpanded] = useState(false);
  const detailsText = (match.summary?.trim() || match.content?.trim() || '');
  const hasLongSummary = detailsText.length > SUMMARY_PREVIEW_LIMIT;
  const displayedSummary = expanded || !hasLongSummary
    ? detailsText
    : `${detailsText.slice(0, SUMMARY_PREVIEW_LIMIT)}...`;

  return (
    <div className="card hover:shadow-md transition-shadow">
      <div className="flex items-start gap-4">
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

          {detailsText && (
            <div className="mb-3">
              <p className="text-sm text-gray-600">{displayedSummary}</p>
              {hasLongSummary && (
                <button
                  type="button"
                  onClick={() => setExpanded((prev) => !prev)}
                  className="mt-2 inline-flex items-center gap-1 text-xs font-medium text-blue-600 hover:text-blue-700"
                >
                  {expanded ? (
                    <>
                      <ChevronUp className="w-3 h-3" />
                      Свернуть
                    </>
                  ) : (
                    <>
                      <ChevronDown className="w-3 h-3" />
                      Смотреть больше
                    </>
                  )}
                </button>
              )}
            </div>
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

      </div>
    </div>
  );
}
